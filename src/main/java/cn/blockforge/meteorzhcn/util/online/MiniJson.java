package cn.blockforge.meteorzhcn.util.online;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MiniJson {
   private final String text;
   private int pos;

   private MiniJson(String text) {
      this.text = text;
   }

   public static Object parse(String text) {
      MiniJson parser = new MiniJson(text);
      parser.skipWhitespace();
      Object value = parser.parseValue();
      parser.skipWhitespace();
      if (parser.pos < parser.text.length()) {
         throw new IllegalArgumentException("JSON 末尾有多余内容");
      } else {
         return value;
      }
   }

   public static Object at(Object node, Object... path) {
      Object current = node;

      for (Object key : path) {
         if (current == null) {
            return null;
         }

         if (key instanceof Integer index && current instanceof List<?> list) {
            current = index >= 0 && index < list.size() ? list.get(index) : null;
         } else {
            if (!(key instanceof String name) || !(current instanceof Map<?, ?> map)) {
               return null;
            }

            current = map.get(name);
         }
      }

      return current;
   }

   public static String quote(String value) {
      StringBuilder sb = new StringBuilder(value.length() + 2);
      sb.append('"');

      for (int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         switch (c) {
            case '\b':
               sb.append("\\b");
               break;
            case '\t':
               sb.append("\\t");
               break;
            case '\n':
               sb.append("\\n");
               break;
            case '\f':
               sb.append("\\f");
               break;
            case '\r':
               sb.append("\\r");
               break;
            case '"':
               sb.append("\\\"");
               break;
            case '\\':
               sb.append("\\\\");
               break;
            default:
               if (c < ' ') {
                  sb.append(String.format("\\u%04x", Integer.valueOf(c)));
               } else {
                  sb.append(c);
               }
         }
      }

      sb.append('"');
      return sb.toString();
   }

   private Object parseValue() {
      char c = this.peek();

      return switch (c) {
         case '"' -> this.parseString();
         case '[' -> this.parseArray();
         case 'f' -> this.parseLiteral("false", Boolean.FALSE);
         case 'n' -> this.parseLiteral("null", null);
         case 't' -> this.parseLiteral("true", Boolean.TRUE);
         case '{' -> this.parseObject();
         default -> this.parseNumber();
      };
   }

   private Map<String, Object> parseObject() {
      this.expect('{');
      Map<String, Object> map = new LinkedHashMap<>();
      this.skipWhitespace();
      if (this.peek() == '}') {
         this.pos++;
         return map;
      } else {
         while (true) {
            this.skipWhitespace();
            String key = this.parseString();
            this.skipWhitespace();
            this.expect(':');
            this.skipWhitespace();
            map.put(key, this.parseValue());
            this.skipWhitespace();
            char c = this.peek();
            if (c != ',') {
               if (c == '}') {
                  this.pos++;
                  return map;
               } else {
                  throw new IllegalArgumentException("JSON 对象里出现意外字符");
               }
            }

            this.pos++;
         }
      }
   }

   private List<Object> parseArray() {
      this.expect('[');
      List<Object> list = new ArrayList<>();
      this.skipWhitespace();
      if (this.peek() == ']') {
         this.pos++;
         return list;
      } else {
         while (true) {
            this.skipWhitespace();
            list.add(this.parseValue());
            this.skipWhitespace();
            char c = this.peek();
            if (c != ',') {
               if (c == ']') {
                  this.pos++;
                  return list;
               } else {
                  throw new IllegalArgumentException("JSON 数组里出现意外字符");
               }
            }

            this.pos++;
         }
      }
   }

   private String parseString() {
      this.expect('"');
      StringBuilder sb = new StringBuilder();

      while (this.pos < this.text.length()) {
         char c = this.text.charAt(this.pos++);
         if (c == '"') {
            return sb.toString();
         }

         if (c != '\\') {
            sb.append(c);
         } else {
            if (this.pos >= this.text.length()) {
               throw new IllegalArgumentException("JSON 转义不完整");
            }

            char esc = this.text.charAt(this.pos++);
            switch (esc) {
               case '"':
                  sb.append('"');
                  break;
               case '/':
                  sb.append('/');
                  break;
               case '\\':
                  sb.append('\\');
                  break;
               case 'b':
                  sb.append('\b');
                  break;
               case 'f':
                  sb.append('\f');
                  break;
               case 'n':
                  sb.append('\n');
                  break;
               case 'r':
                  sb.append('\r');
                  break;
               case 't':
                  sb.append('\t');
                  break;
               case 'u':
                  if (this.pos + 4 > this.text.length()) {
                     throw new IllegalArgumentException("JSON 的 \\u 转义不完整");
                  }

                  sb.append((char)Integer.parseInt(this.text.substring(this.pos, this.pos + 4), 16));
                  this.pos += 4;
                  break;
               default:
                  throw new IllegalArgumentException("不认识的 JSON 转义 " + esc);
            }
         }
      }

      throw new IllegalArgumentException("JSON 字符串没有闭合");
   }

   private Object parseNumber() {
      int start;
      for (start = this.pos; this.pos < this.text.length(); this.pos++) {
         char c = this.text.charAt(this.pos);
         if ((c < '0' || c > '9') && c != '-' && c != '+' && c != '.' && c != 'e' && c != 'E') {
            break;
         }
      }

      if (start == this.pos) {
         throw new IllegalArgumentException("JSON 里出现无法识别的字符");
      } else {
         return Double.parseDouble(this.text.substring(start, this.pos));
      }
   }

   private Object parseLiteral(String literal, Object value) {
      if (!this.text.startsWith(literal, this.pos)) {
         throw new IllegalArgumentException("JSON 里出现无法识别的字符");
      } else {
         this.pos = this.pos + literal.length();
         return value;
      }
   }

   private void skipWhitespace() {
      while (this.pos < this.text.length() && Character.isWhitespace(this.text.charAt(this.pos))) {
         this.pos++;
      }
   }

   private char peek() {
      if (this.pos >= this.text.length()) {
         throw new IllegalArgumentException("JSON 意外结束");
      } else {
         return this.text.charAt(this.pos);
      }
   }

   private void expect(char expected) {
      if (this.peek() != expected) {
         throw new IllegalArgumentException("JSON 里期望字符 " + expected);
      } else {
         this.pos++;
      }
   }
}
