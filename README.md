# CustomFurnace 自定义熔炉插件

> 最后更新：2024年3月21日

## 插件介绍
CustomFurnace是一款为Minecraft服务器设计的高级熔炉插件，允许玩家升级各类熔炉以提高燃烧效率，增强游戏体验。支持使用金币或点券进行升级，提供多种支付方式满足不同玩家需求。特色是金币和点券可以有不同的升级等级上限，为玩家提供更多选择。

## 当前开发状态
✅ 主要功能已完成 - 内测阶段

## 核心功能

### 1. 熔炉升级系统
- 支持自定义无限等级的熔炉
- 每个等级可配置不同的燃烧速度
- 支持自定义升级所需的资源与条件
- **双重支付方式：金币和点券，可独立配置各自最高等级**

### 2. 多类型熔炉支持
- 支持所有原版熔炉类型：普通熔炉、高炉、烟熏炉
- 每种类型可单独配置升级参数
- 每种支付方式可设置不同的最高等级限制

### 3. 交互式升级界面
- 直观的GUI界面用于展示与升级熔炉
- 双重支付选择：金币（金块按钮）或点券（钻石块按钮）
- 实时显示熔炉当前属性与升级后效果
- 不同支付方式显示不同的升级路径和成本

### 4. 全局语言定制
- 完全可自定义的消息系统
- 支持物品名称、描述、界面提示的多语言配置
- 颜色代码与变量支持

### 5. 显示系统
- 支持三种显示方式：
  - 放置状态：使用ArmorStand实现方块上方的悬浮显示
  - 掉落状态：物品掉落时的悬浮显示
  - 手持状态：物品在手中时的悬浮显示
- 每个等级独特的显示名称
- 动态更新显示内容
- 可配置的显示开关

## 实际文件结构
```
CustomFurnace/
├── src/main/java/cn/i7mc/customfurnace/
│   ├── CustomFurnace.java                # 主类
│   ├── managers/
│   │   ├── ConfigManager.java            # 配置管理器
│   │   ├── DataManager.java              # 数据持久化管理器
│   │   ├── EconomyManager.java           # 经济管理器
│   │   ├── FurnaceManager.java           # 熔炉管理器
│   │   └── LangManager.java              # 语言管理器
│   ├── models/
│   │   ├── CustomFurnaceData.java        # 熔炉数据模型
│   │   └── FurnaceLevel.java             # 熔炉等级属性
│   ├── listeners/
│   │   ├── FurnaceListener.java          # 熔炉事件监听
│   │   └── InventoryListener.java        # 物品栏事件监听
│   ├── gui/
│   │   └── FurnaceUpgradeGUI.java        # 升级GUI实现
│   ├── commands/
│   │   └── FurnaceCommand.java           # 命令处理
│   └── utils/
│       ├── MessageUtil.java              # 消息处理工具
│       └── ItemBuilder.java              # 物品构建工具
├── src/main/resources/
│   ├── plugin.yml                        # 插件配置
│   ├── config.yml                        # 主配置
│   ├── furnaces.yml                      # 熔炉配置
│   ├── message.yml                       # 消息配置
│   └── debugmessage.yml                  # 调试消息
├── libs/                                 # 本地依赖库
│   ├── Vault.jar                         # Vault经济API
│   └── PlayerPoints.jar                  # PlayerPoints点券API
└── pom.xml                               # 项目构建文件
```

## 配置设计

### config.yml
```yaml
# 全局设置
debug: false
# 显示设置
display:
  armorstand-hologram: true  # 是否显示盔甲架全息信息
  dropped-item-hologram: true  # 是否显示掉落物悬浮标签
# 经济系统设置
economy:
  use-vault: true  # 是否使用Vault经济系统
  use-points: true  # 是否使用点券系统
# 熔炉设置
save-interval: 300  # 数据保存间隔(秒)
```

### furnaces.yml
```yaml
furnaces:
  furnace:  # 普通熔炉
    display_name: # 自定义每级名字
      1: "&6普通熔炉"
      2: "&6高级熔炉"
      3: "&6强化熔炉"
      4: "&6钛金熔炉"
      5: "&6钻石熔炉"
    vault:  # 金币升级相关配置
      max-level: 3  # 金币最高可升级等级
      speed:  # 各等级燃烧速度(tick)
        1: 200  # 默认
        2: 180
        3: 160
      upgrade-cost:  # 金币升级消耗
        1-2: 1000
        2-3: 3000
    points:  # 点券升级相关配置
      max-level: 5  # 点券最高可升级等级
      speed:  # 各等级燃烧速度(tick)
        1: 200  # 默认
        2: 180
        3: 160
        4: 140
        5: 120
      upgrade-cost:  # 点券升级消耗
        1-2: 100
        2-3: 300
        3-4: 600
        4-5: 1000
```

### message.yml
```yaml
prefix: "&8[&6Custom&fFurnace&8] "
furnace:
  name: "&6高级熔炉"
  hover_display: "&f%display% &7[&d%level%级&7] &7[&e%payment_type%&7] &7[&a%speed%tick&7]"  # 悬浮显示格式
  lore:
    - "&8&m-------------"
    - "&f等级: &d%level%"
    - "&f速度: &a%speed%"
    - "&f类型: &e%payment_type%"
    - "&8&m-------------"
gui:
  title: "熔炉升级"
  upgrade-vault: "&a使用金币升级"
  upgrade-points: "&b使用点券升级"
  upgrade-cost-vault: "&f升级消耗: &e%cost%金币"
  upgrade-cost-points: "&f升级消耗: &b%cost%点券"
  max-level: "&c已达最高等级"
```

## 开发进度表

| 阶段 | 功能模块 | 状态 |
|------|---------|------|
| 1 | 项目框架搭建 | ✅ 已完成 |
| 1 | 配置文件设计 | ✅ 已完成 |
| 2 | 熔炉数据模型 | ✅ 已完成 |
| 2 | 熔炉管理核心 | ✅ 已完成 |
| 3 | 事件监听实现 | ✅ 已完成 |
| 3 | 熔炉速度调整 | ✅ 已完成 |
| 4 | GUI界面设计 | ✅ 已完成 |
| 4 | 升级功能实现 | ✅ 已完成 |
| 5 | 多熔炉类型支持 | ✅ 已完成 |
| 5 | 命令系统实现 | ✅ 已完成 |
| 6 | 数据持久化 | ✅ 已完成 |
| 7 | 双重支付系统框架 | ✅ 已完成 |
| 7 | 支付方式独立等级配置 | ✅ 已完成 |
| 8 | 集成Vault经济系统 | ✅ 已完成 |
| 8 | 集成PlayerPoints系统 | ✅ 已完成 |
| 9 | 熔炉UUID系统 | ✅ 已完成 |
| 10 | 熔炉显示系统 | ✅ 已完成 |
| 11 | 显示系统配置化 | ✅ 已完成 |
| 12 | 配置重载优化 | ✅ 已完成 |
| 13 | 测试与Bug修复 | 🔄 进行中 |

## 实现的功能组件

### 1. 配置与语言
- 完整的配置文件系统
- 可定制的语言消息
- 调试消息系统
- 每个等级独立的显示名称配置
- 显示系统配置化（可开关盔甲架全息和掉落物悬浮标签）

### 2. 熔炉系统
- 熔炉速度调整
- 燃烧效率提升
- 熔炉自定义属性保存
- 支持金币和点券不同等级上限
- 熔炉显示系统（悬浮文本）

### 3. GUI系统
- 交互式熔炉升级界面
- 双重支付选择（金币/点券）
- 实时显示升级成本与效果
- 直观的支付方式选择按钮
- 升级后GUI自动更新

### 4. 命令系统
- 完整的命令与权限系统
- Tab自动补全支持

### 5. 数据持久化
- 熔炉数据自动保存
- 服务器重启后数据恢复
- 可配置的保存间隔
- 悬浮显示实体UUID存储

### 6. 经济系统整合
- Vault经济系统支持（金币支付）
- PlayerPoints点券系统支持（点券支付）
- 灵活配置不同货币类型的升级成本和等级上限

### 7. 显示系统
- 放置状态悬浮显示
- 掉落物品悬浮显示
- 每个等级独特的显示名称
- 动态更新显示内容
- 可配置的显示开关

## 待实现功能
1. 优化GUI界面，添加更多视觉反馈
2. 完善命令系统，支持更多操作和权限控制
3. 全面测试各种边缘情况和兼容性
4. 添加更多自定义配置选项
5. 优化性能和内存使用

## API接口

插件将提供以下API接口供其他插件使用：

- 获取熔炉等级信息
- 修改熔炉等级
- 创建指定等级的熔炉
- 获取熔炉处理速度
- 检查并扣除玩家升级费用

## 使用说明

### 主要命令
- `/furnace help` - 显示帮助信息
- `/furnace info` - 查看手持熔炉信息
- `/furnace upgrade` - 打开升级界面
- `/furnace give <玩家> <类型> <等级> [支付方式]` - 给予玩家指定熔炉
- `/furnace reload` - 重载配置文件

### 权限节点
- `customfurnace.use` - 使用基础功能
- `customfurnace.upgrade` - 升级熔炉权限
- `customfurnace.upgrade.vault` - 使用金币升级权限
- `customfurnace.upgrade.points` - 使用点券升级权限
- `customfurnace.admin` - 管理员权限

## 兼容性与要求
- 支持版本: Spigot/Paper 1.21.4+
- Java版本: Java 21+
- 必要依赖: 
  - Vault (金币经济支持)
  - PlayerPoints (点券系统支持) 

## 最近更新
- 优化了配置重载系统，修复了重载时的栈溢出问题
- 添加了显示系统的配置选项，可在config.yml中控制盔甲架全息和掉落物悬浮标签的显示
- 优化了配置管理器的结构，使其更加清晰和可维护
- 改进了语言管理器的重载机制，避免循环调用
- 完善了熔炉数据持久化，包括悬浮显示实体的UUID存储
- 修复了服务器重启后熔炉全息显示消失的问题
- 修复了高等级熔炉（4-5级，使用点券升级）被破坏后不掉落物品的问题
- 升级命令系统，支持`/furnace give <玩家> <类型> <等级> [支付方式]`格式
- 优化配置文件加载逻辑，消除启动时的警告信息
- 美化控制台输出，添加彩色日志信息
- 修复了升级熔炉后GUI自动关闭的问题
- 添加了updateUpgradeButtons方法用于刷新GUI中的升级按钮状态
- 优化了InventoryListener类，使其支持实时更新GUI显示
- 实现了完整的经济系统集成，包括金币和点券的扣除和余额查询
- 更新了Java版本要求为Java 21
- 添加了本地依赖库支持 