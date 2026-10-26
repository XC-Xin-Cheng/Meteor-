# Meteor Addon+++

Meteor Client 整合 addon:把 Meteor Rejects、Meteor Litematica Printer、Meteor+、
Meteor Satellite Addon、Meteorist、Jefff's Mods 与 Meteor-I18n-XC 简体中文汉化合为一个
Meteor 客户端附加模组。支持 Minecraft 26.1–26.2,统一收入 "Meteor++" 分类并内置简体中文。

> 本仓库源码由 Vineflower 1.11.1 从 `Meteor-Addon+++-26.1.jar` 反编译恢复
> (995 个 .java,0 个反编译失败),Minecraft 类名为官方 Mojang 映射。
> 属于参考级源码,若要重新构建需补上 Fabric addon 模板的 `build.gradle` 等构建文件。

## 源码包结构(`src/main/java`)

| 包 | 对应子 Mod |
|---|---|
| `meteoraddonppp` | **Meteor Addon+++ 本体**(整合入口、mixin) |
| `anticope.rejects` | Meteor Rejects(58 个 modules、17 个 commands) |
| `com.kkllffaa.meteor_litematica_printer` | Meteor Litematica Printer |
| `nekiplay.meteorplus` | Meteor+(含 nofall 等模式) |
| `fr.crazycat256.satellite` | Meteor Satellite Addon |
| `zgoly.meteorist` | Meteorist |
| `cn.blockforge.meteorzhcn` | Meteor-I18n-XC 简体中文汉化 |
| `com.stash.hunt` | Jefff's Mods |
| `com.seedfinding.*` / `cubitect` / `org.java_websocket` / `org.fusesource.hawtjni` | 打入的依赖库(SeedCrackerX 相关:cubiomes、seedfinding、WebSocket) |

## 资源文件(`src/main/resources`)

- `fabric.mod.json`(8 个 meteor 入口、11 个 mixin 配置)、5 个 `*.accesswidener`
- `META-INF/jars/seedcrackerx-api-2.10.1.jar`(嵌套 jar)、`META-INF/native/`(cubiomes 本地库)
- `assets/` 下为各子 mod 的语言文件、贴图、模型
- `ATTRIBUTION.md` 与各 `LICENSE_*`:整合的子 mod 各自保留其许可证(GPL-3.0)

## 许可证

GPL-3.0(所整合 addon 保留各自原许可证,见 `ATTRIBUTION.md`)。
