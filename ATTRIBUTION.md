# Meteor Addon+++ · 致谢与许可（ATTRIBUTION）

Meteor Addon+++ 是把多个开源 Meteor Client addon 的构建产物合并为一个 mod 的整合包，
除整合层（分类重映射、默认设置、致谢过滤）外未修改任何组件逻辑。
各组件仍按其原许可证分发，请遵守 GPL-3.0 / MIT 等条款。

## Minecraft 26.1 完整版（7 个组件）

对应发布：[v26.1](https://github.com/XC-Xin-Cheng/Meteor-/releases/tag/v26.1)。

| 组件 | 原作者 | 版本 | 许可 |
| --- | --- | --- | --- |
| Meteor Rejects | Cloudburst、StormyBytes | 0.3 | GPL-3.0 |
| Meteor Litematica Printer | kkllffaa | 1.5.0 | 见原仓库 |
| Meteor+（MeteorPlus） | Neki_play | 26.1.2_1.0.9.5 | GPL-3.0 |
| Meteor Satellite Addon | crazycat256 | 26.1 | 见原 jar 内 LICENSE |
| Meteorist | Zgoly | 26.1 | MIT |
| Jefff's Mods（Jefff 模组） | jefff | 0.16.7-26.1.2 | 见原仓库 |
| Meteor-I18n-XC（简体中文汉化） | XC_Xin_Cheng | 1.0.0-r36 | 见原作者说明 |

目标环境：Minecraft 26.1 – 26.1.2 + Fabric Loader 0.19.2+ + Meteor Client 26.1.2-42 + Java 25。

## Minecraft 26.2 专版（4 个组件）

| 组件 | 来源 / 原作者 | 版本 | 许可 |
| --- | --- | --- | --- |
| Meteor Rejects | AidenRTRRgfdm 的 26.2 社区移植（原作者 Cloudburst、StormyBytes） | 0.3.1+26.2 | GPL-3.0 |
| Meteor+（MeteorPlus） | Neki_play | 26.2_1.0.9.5 | GPL-3.0 |
| Meteorist | Zgoly | 26.2-1 | MIT |
| Meteor-I18n-XC（简体中文汉化） | XC_Xin_Cheng | 1.0.0-r31 | 见原作者说明 |

目标环境：Minecraft 26.2 + Fabric Loader 0.19.3+ + 与 MC 26.2 匹配的 Meteor Client + Java 25。

26.2 专版暂未包含 Meteor Satellite Addon、Meteor Litematica Printer、Jefff's Mods：
它们没有 26.2 构建，26.1.2 版本在 26.2 上会因 API 变动（EntityTypes / ColorCollection 等）崩溃。

## 原项目地址

- https://github.com/AntiCope/meteor-rejects
- https://github.com/kkllffaa/meteor-litematica-printer
- https://github.com/MeteorClientPlus/MeteorPlus
- https://github.com/crazycat256/meteor-satellite-addon
- https://github.com/Zgoly/Meteorist
- https://github.com/miles352/meteor-stashhunting-addon
- 26.2 Rejects 移植：https://github.com/AidenRTRRgfdm/meteor-rejects

## 整合 / 打包

- 整合、打包：**DSv4.1F、XC_Xin_Cheng**
- 合并后 mod id 为 `meteor-addon-ppp`；各组件仍作为独立 Meteor addon 注册
  （保证模块归属、汉化键、配置兼容），但在 Meteor 标题界面被合并为一条
  “Meteor Addon+++ by DSv4.1F & XC_Xin_Cheng” 致谢。
- 组件原有的 GitHub 版本检查在合并后不再有意义，已随致谢条目一并隐藏。
- 各子 mod 原有的许可证文本保留在 jar 内（`src/main/resources/LICENSE_*`）。

## 默认设置（每次启动自动应用）

- 关闭 Meteor 的「自定义字体（Custom Font）」；
- 自动启用「汉化」模块；
- 自动打开「汉化」模块里的「自动翻译」。

这三项由 `MeteorAddonPPPPostInit` 在游戏加载完语言表后应用（会覆盖配置里的开关）。

## 1.5.8 新增收录

| 组件 | 原作者 | 版本基线 | 许可 |
| --- | --- | --- | --- |
| Asteroide | asteroide-development | 0.2.3 | Apache-2.0 |
| Nora Tweaks | noramibu | build-7/8/11 | CC0-1.0 |
| Stardust | 0xTas | 1.7.0 / 1.14.0 | GPL-3.0 |
| Nerv Printer | Julflips | 1.21.8 / 1.21.11 | GPL-3.0 |
| Hyperglide | Ark223 | 2026.10.20 | GPL-3.0 |

- https://github.com/asteroide-development/Asteroide
- https://github.com/noramibu/Nora-Tweaks
- https://github.com/0xTas/stardust
- https://github.com/Julflips/nerv-printer-addon
- https://github.com/Ark223/Hyperglide

各版本收录数量以上方分支表对应的 jar 为准(上游未出构建的版本做部分收录)。
catppuccin 主题 addon 曾在 1.1.0 系列收录,1.5.8 起移除(其 widget mixin 在整合环境下
导致设置界面异常与主题切换崩溃)。
