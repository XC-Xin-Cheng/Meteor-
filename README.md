# Meteor Addon+++

Meteor Client 整合 addon:把 Meteor Rejects、Meteor Litematica Printer、Meteor+、
Meteor Satellite Addon、Meteorist、Jefff's Mods、Asteroide、Nora Tweaks、Stardust、
Nerv Printer、Hyperglide 与 Meteor-I18n-XC 简体中文汉化合为一个 Meteor 客户端附加模组。
支持 Minecraft 1.21.1–26.3(11 个版本,见下方分支表),统一收入 "Meteor+++" 分类并内置简体中文。
当前版本 **1.5.8**。

> 本仓库源码由 Vineflower 1.11.1 从 `Meteor-Addon+++-26.1.jar` 反编译恢复
> (995 个 .java,0 个反编译失败),Minecraft 类名为官方 Mojang 映射。
> 属于参考级源码;构建文件已按 [Meteor addon 模板](https://github.com/MeteorDevelopment/meteor-addon-template)
> 补齐(Gradle 9.8.1 / Fabric Loom / version catalog),可作为重新构建的起点,
> 反编译产物重建时可能仍需少量修正。

## 下载

**按版本分支下载**:每个支持的 Minecraft 版本一个分支,对应 jar 在分支的 `jar/` 目录内。
前置(Meteor Client、Baritone、Fabric API、Xaero 系列等)**不随仓库分发**,请自行安装
匹配版本;与 Meteor Client 一同放进 `.minecraft/mods`(先删除旧的各子 addon jar 与独立的
Meteor-I18n-XC,避免类重复)。

| 分支 | jar(在 `jar/` 目录内) | 适用 Minecraft |
|---|---|---|
| [1.21.1](https://github.com/XC-Xin-Cheng/Meteor-/tree/1.21.1) | `Meteor-Addon+++-1.21.1-1.5.8.jar` | 1.21.1 |
| [1.21.4](https://github.com/XC-Xin-Cheng/Meteor-/tree/1.21.4) | `Meteor-Addon+++-1.21.4-1.5.8.jar` | 1.21.4 |
| [1.21.5](https://github.com/XC-Xin-Cheng/Meteor-/tree/1.21.5) | `Meteor-Addon+++-1.21.5-1.5.8.jar` | 1.21.5 |
| [1.21.6](https://github.com/XC-Xin-Cheng/Meteor-/tree/1.21.6) | `Meteor-Addon+++-1.21.6-1.5.8.jar` | 1.21.6 |
| [1.21.7](https://github.com/XC-Xin-Cheng/Meteor-/tree/1.21.7) | `Meteor-Addon+++-1.21.7-1.5.8.jar` | 1.21.7 |
| [1.21.8](https://github.com/XC-Xin-Cheng/Meteor-/tree/1.21.8) | `Meteor-Addon+++-1.21.8-1.5.8.jar` | 1.21.8 |
| [1.21.10](https://github.com/XC-Xin-Cheng/Meteor-/tree/1.21.10) | `Meteor-Addon+++-1.21.10-1.5.8.jar` | 1.21.10 |
| [1.21.11](https://github.com/XC-Xin-Cheng/Meteor-/tree/1.21.11) | `Meteor-Addon+++-1.21.11-1.5.8.jar` | 1.21.11 |
| [26.1](https://github.com/XC-Xin-Cheng/Meteor-/tree/26.1) | `Meteor-Addon+++-26.1-1.5.8.jar` | 26.1 – 26.1.2 |
| [26.2](https://github.com/XC-Xin-Cheng/Meteor-/tree/26.2) | `Meteor-Addon+++-26.2-1.5.8.jar` | 26.2 |
| [26.3](https://github.com/XC-Xin-Cheng/Meteor-/tree/26.3) | `Meteor-Addon+++-26.3-1.5.8.jar` | 26.3 |

通用要求:Fabric Loader ≥ 0.19.2(1.21.x 建议 ≥ 0.16.9,26.2+ ≥ 0.19.3)、Java 21(1.21.x)
或 Java 25(26.x)、与游戏版本匹配的 Meteor Client;**建议同时安装匹配版本的 Baritone**
(部分组件初始化引用其内部类)。整合内容、原 addon 版本与许可见 [ATTRIBUTION.md](ATTRIBUTION.md)。

> 各版本收录的组件数量不同(上游没出构建的版本做部分收录):1.21.11 最全(10 组件),
> 26.1 有 8 个,1.21.7 最少(3 个)。**1.5.8 起移除了 catppuccin 主题类**(其 widget mixin
> 在整合环境下导致设置界面异常与主题切换崩溃)。

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

## 从源码构建

```bash
./gradlew build
```

构建文件基于 [Meteor addon 模板](https://github.com/MeteorDevelopment/meteor-addon-template):
`gradle/libs.versions.toml` 中 `minecraft = 26.1.2`、`meteor = 26.1.2-SNAPSHOT`(Meteor Maven snapshots)、
JDK 25。产物输出到 `build/libs/meteor-addon-ppp-<版本>.jar`。

## 许可证

[GPL-3.0](LICENSE)(所整合 addon 保留各自原许可证,见 [ATTRIBUTION.md](ATTRIBUTION.md);
jar 内许可证文本见 `src/main/resources/LICENSE_*`)。
