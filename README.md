# I Should Eat More（吃不能停）

NeoForge 模组，主题是「吃得越多越强」：饱食度与饱和度参与战斗与防御，鼓励玩家多吃、吃多样食物。

---

## 核心机制

### 饱食系统改动（Mixin）

- **饮食效果增强**：吃下的食物会根据「近期饮食种类」和食物营养值进行缩放。系统会记录最近吃过的食物类型，种类（根据食物恢复的饱食度和饱和度数值区分种类）越多样，加成越高（约 1～2 倍）。
- **始终显示需要进食**：`needsFood()` 恒为 true，用于配合本 mod 的饱食度逻辑。

### 进食与界面

- **瞬间进食**：食用任意食物时，进食时间被设为 0，即吃即生效。
- **自定义饱食 HUD**：在饱食图标旁显示当前饱食度与饱和度，格式为 `*饱食度 (+饱和度)`。

---

## 食物剑（Food Sword）

共 5 个等级，均为「按饱食度加伤、攻击时消耗饱食度/饱和度」的剑：

| 剑         | 材质等级 | 额外伤害倍率 |
|------------|----------|--------------|
| 肥肉剑     | 木       | 1×           |
| 肌肉剑     | 石头     | 2×           |
| 骨剑       | 铁       | 1.5×         |
| 胳膊剑     | 钻石     | 4×           |
| 附魔金胳膊剑 | 下界合金 | 6×           |

- **额外伤害**：基础量 = `(log10(饱食度))²`，再乘以上表倍率；饱食度 &lt; 10 时无加成。
- **攻击消耗**：每次命中敌人会按 `log10(饱食度)` 消耗饱和度，不足时再扣饱食度。

---

## 食物盔甲（Food Armor）

五套盔甲（肥肉 / 肌肉 / 骨头 / 胳膊 / 金胳膊），每套 4 件（头盔、胸甲、护腿、靴子）。材质等价于：金、链甲、铁、钻石、下界合金。

### 伤害减免

- 穿戴任意一件「食物盔甲」时，受到伤害会优先用**饱和度 + 饱食度**抵伤。
- 可减免比例有上限，由当前穿戴的套装档次决定（混搭则按四件平均）：
  - 肥肉套：最多 30%
  - 肌肉套：最多 50%
  - 骨头套：最多 70%
  - 胳膊套：最多 85%
  - 金胳膊套：最多 100%
- 仅当 `饱食度 ≥ 100`（即饱食度足够高）时才会触发减免；先扣饱和度，再扣饱食度。

### 空中悬浮（护腿）

- 穿着任意一件**食物护腿**（本 mod 的 leggings）时，如果`饱食度 ≥ 1000`, 可在空中长按跳跃实现**水平悬浮**。
- 每tick会消耗少量饱和度（0.01）；不会消耗饱食度。

---

## 肥肉 → 肌肉

- **肥肉剑**在耐久耗尽被「破坏」时，会变成一把**肌肉剑**（手中替换）。
- **肥肉套**（头盔/胸甲/护腿/靴子）任一件耐久耗尽时，该槽位会变成对应的**肌肉套**部件。
- 物品提示中会注明：「肥肉在锻炼后会变成肌肉。」

---

## 创造模式标签页

所有本 mod 的剑与盔甲都会出现在创造模式标签 **「I should eat more」/「吃不能停」** 中，位置在战斗标签之前。

---

## 依赖与开发

- **Minecraft** + **NeoForge**（见 `neoforge.mods.toml` 版本范围）
- **Appleskin**（必需依赖）
- 开发/构建：使用项目自带的 Gradle（如 `gradlew build`）；若 IDE 缺库可先执行 `gradlew --refresh-dependencies`。

---

## 作者与许可

- 作者：godluck
- 模组描述：Eat as much as you could.

映射与许可等说明见仓库内 `TEMPLATE_LICENSE.txt` 及 [NeoForged 文档](https://docs.neoforged.net/)。

---

# I Should Eat More — English

A NeoForge mod themed around “the more you eat, the stronger you get”: food level and saturation are used in combat and defense, encouraging players to eat more and to eat a variety of foods.

---

## Core mechanics

### Food system changes (Mixin)

- **Stronger eating effects**: Food you eat is scaled by “recent diet variety” and the food’s nutrition. The mod tracks recently eaten food types (by nutrition + saturation values). The more variety, the higher the bonus (about 1×–2×).
- **Always “needs food”**: `needsFood()` is always true, to match this mod’s food-level logic.

### Eating and UI

- **Instant eating**: Any food is consumed immediately (eat time set to 0).
- **Custom food HUD**: Next to the food icon, the mod shows current food level and saturation as `*foodLevel (+saturation)`.

---

## Food Swords

Five tiers of swords that all add damage based on food level and consume food/saturation when hitting:

| Sword           | Material tier | Bonus damage multiplier |
|-----------------|---------------|--------------------------|
| Fat Sword       | Wood          | 1×                       |
| Muscle Sword    | Stone         | 2×                       |
| Bone Sword      | Iron          | 1.5×                     |
| Arm Sword       | Diamond       | 4×                       |
| Golden Arm Sword| Netherite     | 6×                       |

- **Bonus damage**: Base = `(log10(foodLevel))²`, then multiplied by the table above; no bonus when food level &lt; 10.
- **On hit**: Each hit consumes saturation (and then food if needed) by `log10(foodLevel)`.

---

## Food Armor

Five sets (Fat / Muscle / Bone / Arm / Golden Arm), four pieces each (helmet, chestplate, leggings, boots). Material tiers match: gold, chainmail, iron, diamond, netherite.

### Damage reduction

- While wearing any Food Armor, incoming damage is reduced by spending **saturation + food level**.
- The maximum reduction depends on the set tier (mixed sets use the average of the four slots):
  - Fat: up to 30%
  - Muscle: up to 50%
  - Bone: up to 70%
  - Arm: up to 85%
  - Golden Arm: up to 100%
- Reduction only applies when **food level ≥ 100**. Saturation is consumed first, then food level.

### Hover (leggings)

- While wearing any **Food Armor leggings**, if **food level ≥ 1000**, holding jump in mid-air lets you **hover** (sustained lift).
- Costs a small amount of saturation per tick (0.01); food level is not consumed.

---

## Fat → Muscle

- When a **Fat Sword** is broken (durability 0), it is replaced by a **Muscle Sword** in that hand.
- When any **Fat Armor** piece (helmet/chestplate/leggings/boots) breaks, that slot is filled with the corresponding **Muscle** piece.
- Tooltips note: “Fat will turn into muscle after exercise.”

---

## Creative tab

All mod swords and armor appear in the creative tab **“I should eat more”**, placed before the Combat tab.

---

## Dependencies and development

- **Minecraft** + **NeoForge** (see version ranges in `neoforge.mods.toml`)
- **Appleskin** (required)
- Build: use the project’s Gradle (e.g. `gradlew build`). If the IDE is missing libraries, run `gradlew --refresh-dependencies` first.

---

## Author and license

- Author: godluck
- Mod description: Eat as much as you could.

For mapping and license details, see `TEMPLATE_LICENSE.txt` in the repo and [NeoForged docs](https://docs.neoforged.net/).
