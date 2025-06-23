# 🔥 CustomFurnace

<div align="center">

![Minecraft](https://img.shields.io/badge/Minecraft-1.20+-green.svg)
![Paper](https://img.shields.io/badge/Paper-API-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)

**一款功能强大的Minecraft自定义熔炉插件**

*支持多种熔炉类型、双重升级系统、全息图显示和进度条*

</div>

---

## 📋 目录

- [✨ 功能特性](#-功能特性)
- [🎯 支持的熔炉类型](#-支持的熔炉类型)
- [💰 升级系统](#-升级系统)
- [🖥️ GUI界面](#️-gui界面)
- [📊 全息图显示](#-全息图显示)
- [⚙️ 安装配置](#️-安装配置)
- [🎮 命令系统](#-命令系统)
- [🔐 权限系统](#-权限系统)
- [📁 配置文件](#-配置文件)
- [🔧 开发信息](#-开发信息)

---

## ✨ 功能特性

### 🏭 多种熔炉类型
- **普通熔炉** (Furnace) - 基础冶炼设备
- **高炉** (Blast Furnace) - 快速金属冶炼
- **烟熏炉** (Smoker) - 食物烹饪专用

### 💎 双重升级系统
- **💰 金币升级** - 使用Vault经济系统
- **🎫 点券升级** - 使用PlayerPoints系统
- **📈 等级差异** - 不同支付方式有不同的最高等级限制

### 🎨 可视化界面
- **GUI升级界面** - 直观的升级操作
- **全息图显示** - 实时显示熔炉信息
- **进度条系统** - 动态显示烧制进度

### 🛠️ 高级功能
- **数据持久化** - 使用PersistentDataContainer技术
- **自动保存** - 定时保存熔炉数据
- **调试模式** - 完整的调试信息输出
- **多语言支持** - 完全可配置的消息系统

---

## 🎯 支持的熔炉类型

| 熔炉类型 | 金币最高等级 | 点券最高等级 | 基础速度 | 特殊用途 |
|---------|-------------|-------------|----------|----------|
| 🔥 **普通熔炉** | 3级 | 5级 | 200 tick | 通用冶炼 |
| ⚡ **高炉** | 2级 | 3级 | 100 tick | 金属冶炼 |
| 🍖 **烟熏炉** | 2级 | 3级 | 100 tick | 食物烹饪 |

### 📊 等级效果对比

**普通熔炉升级效果：**
- 1级：200 tick (默认速度)
- 2级：180 tick (提升11%)
- 3级：160 tick (提升25%)
- 4级：140 tick (仅点券，提升43%)
- 5级：120 tick (仅点券，提升67%)

---

## 💰 升级系统

### 💳 金币升级 (Vault)
| 熔炉类型 | 1→2级 | 2→3级 | 3→4级 | 4→5级 |
|---------|-------|-------|-------|-------|
| 普通熔炉 | 1,000金币 | 3,000金币 | ❌ | ❌ |
| 高炉 | 2,000金币 | ❌ | ❌ | ❌ |
| 烟熏炉 | 2,000金币 | ❌ | ❌ | ❌ |

### 🎫 点券升级 (PlayerPoints)
| 熔炉类型 | 1→2级 | 2→3级 | 3→4级 | 4→5级 |
|---------|-------|-------|-------|-------|
| 普通熔炉 | 100点券 | 300点券 | 600点券 | 1,000点券 |
| 高炉 | 200点券 | 500点券 | ❌ | ❌ |
| 烟熏炉 | 200点券 | 500点券 | ❌ | ❌ |

---

## 🖥️ GUI界面

### 🎮 升级界面布局
```
┌─────────────────────────────┐
│  □  □  □  □  🔥  □  □  □  □  │  ← 第1行
│  □  □  □  💰  □  🎫  □  □  □  │  ← 第2行  
│  □  □  □  □  □  □  □  □  □  │  ← 第3行
└─────────────────────────────┘
```

- **🔥 中央位置** - 显示当前熔炉
- **💰 金币升级** - 左侧升级按钮
- **🎫 点券升级** - 右侧升级按钮
- **□ 装饰边框** - 灰色玻璃板填充

### 🎨 按钮状态
- **🟢 可升级** - 绿色/蓝色方块，显示升级成本
- **🔴 已满级** - 红色方块，显示"已达最高等级"

---

## 📊 全息图显示

### 🏷️ 熔炉信息显示
```
🔥 高级熔炉 [2级] [金币] [180tick]
🟢 正在烧制...
📊 烧制进度: 65.3%
[████████████░░░] 65%
```

### 🎯 进度条特性
- **📏 长度可配置** - 默认15个字符
- **🌈 颜色渐变** - 绿色→黄色→红色
- **⚡ 实时更新** - 每5tick更新一次
- **🎨 自定义字符** - 可配置进度条样式

---

## ⚙️ 安装配置

### 📋 系统要求
- **Minecraft版本**: 1.20+
- **服务端**: Paper
- **Java版本**: 17+
- **依赖插件**: Vault, PlayerPoints

### 🚀 安装步骤

1. **下载依赖插件**
   ```bash
   # 下载必需插件
   - Vault (经济系统前置)
   - PlayerPoints (点券系统)
   ```

2. **安装插件**
   ```bash
   # 将插件放入plugins文件夹
   plugins/
   ├── Vault.jar
   ├── PlayerPoints.jar
   └── CustomFurnace.jar
   ```

3. **重启服务器**
   ```bash
   # 重启服务器以加载插件
   /restart
   ```

4. **验证安装**
   ```bash
   # 检查插件状态
   /plugins
   # 测试命令
   /furnace help
   ```

---

## 🎮 命令系统

### 📝 基础命令

| 命令 | 描述 | 权限要求 |
|------|------|----------|
| `/furnace help` | 📖 显示帮助信息 | `customfurnace.use` |
| `/furnace info` | ℹ️ 查看手持熔炉信息 | `customfurnace.use` |
| `/furnace upgrade` | ⬆️ 打开升级界面 | `customfurnace.upgrade` |

### 🛠️ 管理员命令

| 命令 | 描述 | 权限要求 |
|------|------|----------|
| `/furnace give <玩家> <类型> <等级> [支付方式]` | 🎁 给予指定熔炉 | `customfurnace.admin` |
| `/furnace reload` | 🔄 重载配置文件 | `customfurnace.admin` |
| `/furnace checkecon` | 💰 检查经济系统状态 | `customfurnace.admin` |

### 💡 命令示例
```bash
# 给予玩家一个3级普通熔炉(金币类型)
/furnace give Steve furnace 3 vault

# 给予玩家一个5级普通熔炉(点券类型)  
/furnace give Alex furnace 5 points

# 给予玩家一个2级高炉(默认金币类型)
/furnace give Bob blast_furnace 2
```

---

## 🔐 权限系统

### 👥 基础权限
- **`customfurnace.use`** - 基础使用权限 (默认: true)
- **`customfurnace.upgrade`** - 升级熔炉权限 (默认: true)

### 💰 升级权限
- **`customfurnace.upgrade.vault`** - 金币升级权限 (默认: true)
- **`customfurnace.upgrade.points`** - 点券升级权限 (默认: true)

### 👑 管理员权限
- **`customfurnace.admin`** - 管理员权限 (默认: op)
  - 包含所有上述权限
  - 可使用give、reload、checkecon命令

### 🎯 权限配置示例
```yaml
# 在权限插件中配置
groups:
  default:
    permissions:
      - customfurnace.use
      - customfurnace.upgrade
      - customfurnace.upgrade.vault
  vip:
    permissions:
      - customfurnace.upgrade.points
  admin:
    permissions:
      - customfurnace.admin
```

---

## 📁 配置文件

### ⚙️ config.yml - 主配置文件
```yaml
# 全局设置
debug: false  # 调试模式开关

# 显示设置
display:
  # 全息图设置
  text-display-hologram: true
  text-display-settings:
    alignment: CENTER        # 文本对齐: LEFT/CENTER/RIGHT
    shadowed: true          # 文本阴影
    see-through: true       # 透视显示
    billboard: CENTER       # 广告牌模式
    background-color: "0,0,0,0"  # 背景颜色(ARGB)
    text-opacity: -1        # 文本透明度
    line-width: 200         # 最大行宽
    y-offset: 0.8          # Y轴偏移量

  # 进度条设置
  progress-bar:
    enabled: true           # 启用进度条
    update-interval: 5      # 更新间隔(tick)
    length: 15             # 进度条长度
    character: "I"         # 进度字符
    start-char: "["        # 开始字符
    end-char: "]"          # 结束字符
    border-color: "&8"     # 边框颜色
    filled-colors:         # 填充颜色(渐变)
      - "&a"  # 绿色
      - "&e"  # 黄色
      - "&c"  # 红色
    empty-color: "&7"      # 空白颜色

  # 掉落物显示
  dropped-item-hologram: true

# 经济系统
economy:
  use-vault: true   # 启用Vault
  use-points: true  # 启用PlayerPoints

# 数据保存间隔(秒)
save-interval: 300
```

### 💬 message.yml - 消息配置
```yaml
# 消息前缀
prefix: "&8[&6Custom&fFurnace&8] "

# 插件消息
plugin:
  enable: "CustomFurnace插件已启动"
  disable: "CustomFurnace插件已停止"

# 熔炉相关
furnace:
  name: "&6高级熔炉"
  hologram_hud: "&f%display% &7[&d%level%级&7] &7[&e%payment_type%&7] &7[&a%speed%tick&7]"
  dropped_item_hud: "&f%display% &7[&d%level%级&7] &7[&e%payment_type%&7] &7[&a%speed%tick&7]"
  lore:
    - "&8&m-------------"
    - "&f等级: &d%level%"
    - "&f速度: &a%speed%"
    - "&f类型: &e%payment_type%"
    - "&8&m-------------"

# GUI界面
gui:
  title: "熔炉升级"
  upgrade-vault: "&a使用金币升级"
  upgrade-points: "&b使用点券升级"
  upgrade-cost-vault: "&f升级消耗: &e%cost%金币"
  upgrade-cost-points: "&f升级消耗: &b%cost%点券"
  max-level: "&c已达最高等级"

# 系统消息
messages:
  upgrade-success: "&a熔炉升级成功！"
  upgrade-fail-vault: "&c升级失败，金币不足"
  upgrade-fail-points: "&c升级失败，点券不足"
  not_furnace: "&c你必须手持熔炉才能使用此命令"
  not_custom_furnace: "&c这不是一个自定义熔炉"
  no_permission: "&c你没有权限执行此命令"
  reload_success: "&a配置文件重载成功"
  # ... 更多消息配置
```

### 🔥 furnaces.yml - 熔炉配置
```yaml
furnaces:
  furnace:  # 普通熔炉
    display_name:
      1: "&6普通熔炉"
      2: "&6高级熔炉"
      3: "&6强化熔炉"
      4: "&6钛金熔炉"
      5: "&6钻石熔炉"
    vault:  # 金币升级
      max-level: 3
      speed:
        1: 200
        2: 180
        3: 160
      upgrade-cost:
        1-2: 1000
        2-3: 3000
    points:  # 点券升级
      max-level: 5
      speed:
        1: 200
        2: 180
        3: 160
        4: 140
        5: 120
      upgrade-cost:
        1-2: 100
        2-3: 300
        3-4: 600
        4-5: 1000

  blast_furnace:  # 高炉配置
    # ... 类似结构

  smoker:  # 烟熏炉配置
    # ... 类似结构
```

---

## 🔧 开发信息

### 🏗️ 项目架构
```
src/main/java/cn/i7mc/customfurnace/
├── CustomFurnace.java          # 主类
├── commands/
│   └── FurnaceCommand.java     # 命令处理
├── gui/
│   └── FurnaceUpgradeGUI.java  # GUI界面
├── listeners/
│   ├── FurnaceListener.java    # 熔炉事件监听
│   └── InventoryListener.java  # 库存事件监听
├── managers/
│   ├── ConfigManager.java      # 配置管理
│   ├── DataManager.java        # 数据管理
│   ├── EconomyManager.java     # 经济管理
│   ├── FurnaceManager.java     # 熔炉管理
│   └── LangManager.java        # 语言管理
├── models/
│   ├── CustomFurnaceData.java  # 熔炉数据模型
│   └── FurnaceLevel.java       # 等级数据模型
└── utils/
    ├── ItemBuilder.java        # 物品构建工具
    ├── MessageUtil.java        # 消息工具
    └── TextDisplayUtil.java    # 文本显示工具
```

### 🛠️ 技术特性
- **📦 模块化设计** - 清晰的包结构和职责分离
- **🔄 异步处理** - 数据保存和进度条更新使用异步任务
- **💾 数据持久化** - PersistentDataContainer + YAML文件存储
- **🎨 可配置界面** - 完全可自定义的消息和显示效果
- **🔍 调试支持** - 完整的调试信息输出系统
- **⚡ 性能优化** - ConcurrentHashMap和高效的数据结构

### 📊 API依赖
- **Paper API 1.20** - 主要API框架
- **Vault** - 经济系统集成
- **PlayerPoints** - 点券系统集成

### 🔄 构建信息
```gradle
plugins {
    id 'java'
}

dependencies {
    compileOnly 'io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT'
    compileOnly files('libs/Vault.jar')
    compileOnly files('libs/PlayerPoints.jar')
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}
```

### 📝 版本信息
- **当前版本**: 1.2.0.4
- **API版本**: 1.20
- **构建工具**: Gradle
