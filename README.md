# CustomFurnace 自定义熔炉插件

> 最后更新：2024年12月20日

## 插件介绍
CustomFurnace是一款为Minecraft服务器设计的高级熔炉插件，允许玩家升级各类熔炉以提高冶炼效率，增强游戏体验。支持使用金币(Vault)或点券(PlayerPoints)进行升级，提供双重支付方式满足不同玩家需求。特色是金币和点券可以有不同的升级等级上限，为玩家提供更多选择。插件采用模块化设计，具有完整的数据持久化、悬浮显示、GUI交互等功能。

## 当前开发状态
✅ 主要功能已完成 - 正式版本

## 核心功能

### 1. 熔炉升级系统
- 支持自定义多等级的熔炉（每种支付方式可配置不同最高等级）
- 每个等级可配置不同的冶炼速度（tick数，数值越小速度越快）
- 支持自定义升级所需的资源与条件
- **双重支付方式：金币(Vault)和点券(PlayerPoints)，可独立配置各自最高等级**
- 燃料效率提升：高等级熔炉燃料持续时间更长

### 2. 多类型熔炉支持
- 支持所有原版熔炉类型：普通熔炉(furnace)、高炉(blast_furnace)、烟熏炉(smoker)
- 每种类型可单独配置升级参数和显示名称
- 每种支付方式可设置不同的最高等级限制
- 自动识别熔炉类型并应用对应配置

### 3. 交互式升级界面
- 直观的GUI界面用于展示与升级熔炉（3行9列布局）
- 双重支付选择：金币（金块按钮，槽位11）或点券（钻石块按钮，槽位15）
- 实时显示熔炉当前属性与升级后效果
- 不同支付方式显示不同的升级路径和成本
- 升级成功后自动更新GUI显示和玩家余额提示

### 4. 多语言支持系统
- **动态语言切换**：支持英文(en)和中文(zh)语言配置
- **配置化语言选择**：通过config.yml中的language设置切换语言
- **完整的消息系统**：
  - 英文消息文件（message_en.yml）
  - 中文消息文件（message_zh.yml）
  - 英文调试消息（debugmessage_en.yml）
  - 中文调试消息（debugmessage_zh.yml）
- **实时语言切换**：无需重启服务器，使用/furnace reload即可切换语言
- 颜色代码与变量支持（%level%、%speed%、%payment_type%等）

### 5. 显示系统
- 支持三种显示方式：
  - 放置状态：使用TextDisplay实现方块上方的现代化全息显示（可配置开关）
  - 掉落状态：物品掉落时的悬浮显示（可配置开关）
  - 物品状态：物品在背包中的Lore显示
- 每个等级独特的显示名称（可在furnaces.yml中配置）
- 动态更新显示内容，包含等级、速度、支付方式信息
- 可配置的显示开关（config.yml中的display配置）

### 6. 数据持久化系统
- 使用UUID标识每个熔炉，确保数据唯一性
- 自动保存熔炉数据到data.yml文件
- 可配置的自动保存间隔（默认300秒）
- 服务器重启后自动恢复熔炉状态和悬浮显示
- 兼容旧版本数据格式，自动为旧数据生成UUID

### 7. 经济系统集成
- 完整的Vault经济系统支持（金币支付）
- 完整的PlayerPoints点券系统支持（点券支付）
- 自动检测经济插件可用性并提供状态反馈
- 支持余额检查、扣费操作和余额显示
- 可在config.yml中独立开关两种经济系统

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
│   ├── message_en.yml                    # 英文消息配置
│   ├── message_zh.yml                    # 中文消息配置
│   ├── debugmessage_en.yml               # 英文调试消息
│   └── debugmessage_zh.yml               # 中文调试消息
├── libs/                                 # 本地依赖库
│   ├── Vault.jar                         # Vault经济API
│   └── PlayerPoints.jar                  # PlayerPoints点券API
├── build.gradle                          # Gradle构建文件
├── settings.gradle                       # Gradle设置文件
└── pom.xml                               # Maven构建文件（备用）
```

## 配置设计

### config.yml
```yaml
# 全局设置
debug: false
# 语言设置 (en: 英文, zh: 中文)
language: en
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

### 语言配置文件

#### message_zh.yml (中文)
```yaml
prefix: "&8[&6Custom&fFurnace&8] "
furnace:
  name: "&6高级熔炉"
  hover_display: "&f%display% &7[&d%level%级&7] &7[&e%payment_type%&7] &7[&a%speed%tick&7]"
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

#### message_en.yml (英文)
```yaml
prefix: "&8[&6Custom&fFurnace&8] "
furnace:
  name: "&6Advanced Furnace"
  hover_display: "&f%display% &7[&dLv.%level%&7] &7[&e%payment_type%&7] &7[&a%speed%tick&7]"
  lore:
    - "&8&m-------------"
    - "&fLevel: &d%level%"
    - "&fSpeed: &a%speed%"
    - "&fType: &e%payment_type%"
    - "&8&m-------------"
gui:
  title: "Furnace Upgrade"
  upgrade-vault: "&aUpgrade with Coins"
  upgrade-points: "&bUpgrade with Points"
  upgrade-cost-vault: "&fUpgrade Cost: &e%cost% coins"
  upgrade-cost-points: "&fUpgrade Cost: &b%cost% points"
  max-level: "&cMax level reached"
```

## 实现的功能组件

### 1. 配置与多语言系统
- **动态语言切换系统**：支持英文(en)和中文(zh)
- **配置化语言选择**：通过config.yml的language设置控制
- **完整的消息文件系统**：
  - message_en.yml / message_zh.yml（主要消息）
  - debugmessage_en.yml / debugmessage_zh.yml（调试消息）
- **实时语言切换**：使用/furnace reload命令即可切换语言
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
- Tab自动补全支持（包括玩家名、熔炉类型、等级、支付方式）
- 支持info、upgrade、give、reload、checkecon等子命令
- 详细的帮助信息和错误提示

### 5. 数据持久化
- 熔炉数据自动保存到data.yml
- 服务器重启后数据恢复和悬浮显示重建
- 可配置的保存间隔（默认300秒）
- 悬浮显示实体UUID存储和管理
- 线程安全的数据存储（使用ConcurrentHashMap）

### 6. 经济系统整合
- Vault经济系统支持（金币支付）
- PlayerPoints点券系统支持（点券支付）
- 灵活配置不同货币类型的升级成本和等级上限
- 自动检测经济插件状态并提供反馈
- 支持余额查询和扣费操作

### 7. 显示系统
- 放置状态悬浮显示（TextDisplay现代化实现）
- 掉落物品悬浮显示（Item实体自定义名称）
- 每个等级独特的显示名称
- 动态更新显示内容（包含等级、速度、支付方式）
- 可配置的显示开关（config.yml控制）

### 8. 事件监听系统
- 熔炉放置事件监听（BlockPlaceEvent）
- 熔炉破坏事件监听（BlockBreakEvent）
- 熔炉燃烧事件监听（FurnaceBurnEvent）
- 熔炉冶炼事件监听（FurnaceStartSmeltEvent、FurnaceSmeltEvent）
- GUI交互事件监听（InventoryClickEvent）
- 物品掉落事件监听（PlayerDropItemEvent）

## 实际实现的核心机制

### 熔炉速度调整机制
- 通过监听`FurnaceStartSmeltEvent`事件设置冶炼时间
- 使用`event.setTotalCookTime()`方法调整冶炼速度
- 同时更新熔炉方块状态的`setCookTimeTotal()`确保一致性

### 燃料效率提升机制
- 通过监听`FurnaceBurnEvent`事件调整燃烧时间
- 根据熔炉等级计算燃料效率倍数：`1.0 + (level - 1) * 0.2`
- 每级增加20%的燃料持续时间

### 悬浮显示管理
- 使用TextDisplay实体实现方块上方现代化全息显示
- TextDisplay配置：`setBillboard(CENTER)`、`setShadowed(true)`、`setViewRange(64.0f)`
- 悬浮显示位置：方块中心上方0.9格（`location.add(0.5, 0.9, 0.5)`）
- 支持UUID关联，确保悬浮显示与熔炉数据一致
- 高质量文本渲染，支持颜色和样式，性能优于传统ArmorStand方案

### 数据存储结构
```yaml
# data.yml 结构
furnaces:
  <UUID>:
    type: "furnace"           # 熔炉类型
    level: 2                  # 等级
    uuid: "<UUID>"           # 熔炉UUID
    payment_type: "vault"     # 支付方式
    world: "world"           # 世界名
    x: 100                   # X坐标
    y: 64                    # Y坐标
    z: 200                   # Z坐标
    hologram_uuid: "<UUID>"  # TextDisplay全息显示UUID（可选）
```

## 待优化功能
1. 添加更多视觉反馈和动画效果
2. 支持更多自定义配置选项
3. 优化大量熔炉时的性能表现
4. 添加熔炉统计和监控功能
5. 支持更多经济插件兼容

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
- `/furnace info` - 查看手持熔炉信息（显示类型、等级、速度、升级成本）
- `/furnace upgrade` - 打开升级界面（需要手持熔炉）
- `/furnace give <玩家> <类型> <等级> [支付方式]` - 给予玩家指定熔炉
  - 类型：furnace、blast_furnace、smoker
  - 等级：1到对应支付方式的最高等级
  - 支付方式：vault（金币）或points（点券），默认为vault
- `/furnace reload` - 重载配置文件（重载所有配置并重新应用熔炉数据）
- `/furnace checkecon` - 检查经济系统状态（显示Vault和PlayerPoints的启用状态及玩家余额）

### 权限节点
- `customfurnace.use` - 使用基础功能（默认：true）
- `customfurnace.upgrade` - 升级熔炉权限（默认：true）
- `customfurnace.upgrade.vault` - 使用金币升级权限（默认：true）
- `customfurnace.upgrade.points` - 使用点券升级权限（默认：true）
- `customfurnace.admin` - 管理员权限（默认：op，包含所有权限）

### 多语言配置使用说明

#### 切换语言
1. **设置英文**：在`config.yml`中设置`language: en`
2. **设置中文**：在`config.yml`中设置`language: zh`
3. **应用更改**：执行`/furnace reload`命令重载配置

#### 语言文件说明
- **message_en.yml**：英文界面消息、命令提示、错误信息等
- **message_zh.yml**：中文界面消息、命令提示、错误信息等
- **debugmessage_en.yml**：英文调试信息（当debug: true时显示）
- **debugmessage_zh.yml**：中文调试信息（当debug: true时显示）

#### 自定义语言
可以通过编辑对应的语言文件来自定义消息内容，支持：
- 颜色代码（&a, &c, &f等）
- 变量替换（%level%, %speed%, %cost%等）
- 多行消息（如lore列表）

## 兼容性与要求
- 支持版本: Paper 1.20.1+ (推荐使用Paper而非Spigot)
- Java版本: Java 17+
- 构建系统: Gradle (主要) / Maven (备用)
- 必要依赖:
  - Vault (金币经济支持)
  - PlayerPoints (点券系统支持)

## 技术特点
- **模块化设计**：使用管理器模式分离配置、数据、经济、语言等功能
- **多语言支持**：动态语言切换系统，支持英文和中文
- **事件驱动**：监听方块放置、破坏、熔炉燃烧等事件
- **数据持久化**：使用UUID标识熔炉，支持数据保存和恢复
- **线程安全**：使用ConcurrentHashMap确保数据操作的线程安全
- **配置灵活**：支持自定义每级熔炉的名称、速度、升级成本
- **调试支持**：完整的调试消息系统，便于开发和维护
- **向后兼容**：支持旧版本数据格式，自动迁移和UUID生成
- **Paper API优先**：使用Paper 1.20.1 API，避免反射调用
- **现代构建系统**：支持Gradle构建，优化依赖管理

## 调试和开发
### 调试模式
在`config.yml`中设置`debug: true`可启用调试模式，将输出详细的调试信息：
- 熔炉放置和破坏事件
- 升级操作和经济交易
- 数据加载和保存过程
- GUI交互和事件处理
- 悬浮显示创建和移除

### 控制台输出
插件启动时会显示：
- 各管理器初始化状态
- 经济系统连接状态（Vault和PlayerPoints）
- 配置文件加载结果
- 熔炉数据恢复统计

### 常见问题排查
1. **熔炉不升级**：检查经济插件是否正确安装和配置
2. **悬浮显示不出现**：确认`config.yml`中`display.armorstand-hologram`为true
3. **数据丢失**：检查`data.yml`文件权限和`save-interval`配置
4. **GUI不响应**：确认玩家有相应权限且手持正确的熔炉物品

## 最近更新

### 2024年12月20日
- ✅ **实现多语言支持系统**：
  - 添加了动态语言切换功能（英文/中文）
  - 创建了完整的英文消息文件（message_en.yml, debugmessage_en.yml）
  - 修改了ConfigManager支持语言配置动态加载
  - 支持通过config.yml的language设置切换语言
- ✅ **构建系统现代化**：
  - 从Maven迁移到Gradle构建系统
  - 将Java版本从21降级到17以提高兼容性
  - 将核心依赖从Spigot 1.21.4改为Paper 1.20.1
  - 正确配置了本地依赖（Vault.jar, PlayerPoints.jar）
  - 成功构建并生成CustomFurnace-1.21.4.jar

### 历史更新
- ✅ 完成了完整的双支付系统（金币+点券）
- ✅ 实现了UUID熔炉标识系统，确保数据唯一性
- ✅ 添加了完整的悬浮显示系统（TextDisplay全息+掉落物悬浮标签）
- ✅ 优化了配置重载系统，修复了重载时的栈溢出问题
- ✅ 完善了熔炉数据持久化，包括悬浮显示实体的UUID存储
- ✅ 修复了服务器重启后熔炉全息显示消失的问题
- ✅ 修复了高等级熔炉被破坏后不掉落物品的问题
- ✅ 升级命令系统，支持完整的Tab补全和参数验证
- ✅ 实现了完整的经济系统集成，包括余额查询和扣费操作
- ✅ 添加了checkecon命令用于检查经济系统状态
- ✅ 优化了GUI交互，支持升级后实时更新显示
- ✅ 完善了事件监听系统，支持所有熔炉相关事件
- ✅ 添加了燃料效率提升功能（高等级熔炉燃料持续时间更长）