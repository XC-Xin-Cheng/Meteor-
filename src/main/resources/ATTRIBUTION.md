# Meteor Addon+++ · 整合说明与致谢

一个 Meteor Client 附加模组，整合以下 7 个组件（原 jar 的全部类、资源、mixin、
access widener 均已合并，模块 / 命令 / HUD 与汉化词库保持原样）：

| 组件 | 原作者 | 版本 | 许可 |
| --- | --- | --- | --- |
| Meteor Rejects | Cloudburst、StormyBytes | 0.3 | GPL-3.0 |
| Meteor Litematica Printer | kkllffaa | 1.5.0 | 见原仓库 |
| Meteor+（MeteorPlus） | Neki_play | 26.1.2_1.0.9.5 | GPL-3.0 |
| Meteor Satellite Addon | crazycat256 | 26.1 | 见原 jar 内 LICENSE |
| Meteorist | Zgoly | 26.1 | MIT |
| Jefff's Mods（Jefff 模组） | jefff | 0.16.7-26.1.2 | 见原仓库 |
| Meteor-I18n-XC（简体中文汉化） | XC_Xin_Cheng | 1.0.0-r36 | 见原作者说明 |

- 整合 / 打包：DSv4.1F、XC_Xin_Cheng
- 目标环境：Minecraft 26.1.2 + Fabric Loader 0.19.2+ + Meteor Client 26.1.2-42 + Java 25
- 原项目地址：
  - https://github.com/AntiCope/meteor-rejects
  - https://github.com/kkllffaa/meteor-litematica-printer
  - https://github.com/MeteorClientPlus/MeteorPlus
  - https://github.com/crazycat256/meteor-satellite-addon
  - https://github.com/Zgoly/Meteorist
  - https://github.com/miles352/meteor-stashhunting-addon

## Jefff's Mods 移植说明

- 原目录里的 `jefff-mod-0.16.7-1.21.11.jar` 是 1.21.11 构建，无法直接在 26.1.2 上运行。
- 作者在源码仓库中提供了 **26.1.2 分支和官方发布**（`jefff-mod-0.16.7-26.1.2.jar`）。
  本包使用该官方 26.1.2 构建，未修改其逻辑，因此属于“按上游移植版合并”，而不是手改字节码。
- 它的 18 个模块（含需要 Baritone / Xaero 的可选模块）全部并入 **Meteor+++** 分类；
  2 个 HUD 元素仍在 HUD 编辑器的“Jefff Mod”分组里。

## 分类

- 新增 Meteor 模块分类 **「Meteor+++」**，放在分类列表最上方。
- Rejects、Litematica Printer、Satellite、Meteorist、Jefff's Mods、汉化（I18n）的
  全部模块（共 118 个）都归入「Meteor+++」；原来的 Rejects / Litematica / Satellite /
  Meteorist / Jefff Mod / XC Meteor汉化 分类已移除。
- **Meteor+ 的模块保持原分类不变**（Combat / Player / Movement / Render / World / Misc）。
- 汉化词库已按新分类重映射并补全 Jefff's Mods 的翻译（共 19065 条键）。

## 使用

1. 删除 mods 里的原 5 个 addon jar、**Meteor-I18n-XC jar** 以及旧的 Jefff 模组 jar
   （避免类重复）；
2. 把 Meteor Addon+++ 的 jar 放进 mods；
3. 建议同时安装 **Modern UI（现代化 UI）**，中文界面字体显示更好；
4. 启动游戏。Meteor 标题界面的致谢只显示一条
   “Meteor Addon+++ by DSv4.1F & XC_Xin_Cheng”（红色 DSv4.1F，灰色斜体 XC_Xin_Cheng）。
5. 汉化：词库已内置。在 Meteor GUI 的「Meteor+++」分类里找到「汉化」模块，
   启用后点击 Translate（或打开 auto-translation）即可翻译全部组件的模块名、
   描述和设置项。

## 默认设置（每次启动自动应用）

- 关闭 Meteor 的「自定义字体（Custom Font）」；
- 自动启用「汉化」模块；
- 自动打开「汉化」模块里的「自动翻译」。

这三项由 `MeteorAddonPPPPostInit` 在游戏加载完语言表后应用（会覆盖配置里的开关）。
如果需要临时关闭自动翻译，可以在本局游戏内手动关掉。

## 说明

- 本包只是把上述开源项目的构建产物合并为一个 mod，未修改任何组件的逻辑；
  各组件仍按原许可分发，请遵守各自许可证。
- 合并后 mod id 为 `meteor-addon-ppp`；7 个组件仍作为独立 Meteor addon
  注册（以保证模块归属、汉化键、配置兼容），但在 Meteor 标题界面中被合并为
  一条 Meteor Addon+++ 致谢。
- 组件原有的 GitHub 版本检查在合并后不再有意义，已随致谢条目一并隐藏。
