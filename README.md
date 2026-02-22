# 吃不能停 I should eat more

## 中文

### 概述

移除饥饿值和饱和度上限，模组提供的剑和盔甲会根据饥饿值增强能力，并消耗额外的饱和度/饥饿值。本模组专注于数值调整。

### 核心机制

- **移除饱和度上限**
- **增加饥饿值的阶段性上限与饥饿值等级**
  - 饥饿值等级为 L 时饥饿值上限为 `10^(L+1)`
- **增加渡劫功能**
  - 饥饿值突破当前等级上限时触发渡劫，玩家会受到雷击伤害，从劫雷中存活下来之后饥饿值等级+1
  - 渡劫共 L 道雷击，每次雷击伤害为 `10^L`，每次雷击间隔一秒
  - 请认真对待劫雷！未能完全承受所有劫雷（服务器重启、离线、卡无敌帧等）可能导致渡劫失败，需要重新渡劫
- **动态调整食物效率**
  - 根据食物提供的饥饿值计算**食物等级 FL**（非整数）：`FL = log10(食物饥饿值)`
  - **鼓励食物多样性**：吃下食物时会根据最近 `L+5` 种「不同数值」（饥饿值与饱和度的不同组合）的食物提供额外的饥饿值/饱和度倍率 **×(1～2)**
    - 计算方式：`(最近食用的 L+5 种食物中不同数值的种类数 / (L+5)) + 1`，即轮流食用 L+5 种食物即可达到最大效率
    - 提供相同饥饿值和饱和度的食物被记为同一种（如 1 饥饿+1 饱和的两种食物属于同一类别）
  - **鼓励食用高级食物**：吃下食物时会根据食物等级提供额外倍率 **×(1+FL)**
  - **降低后期食物效率**：吃下食物时会根据玩家饥饿值等级降低效率，倍率 **×1/L**
  - **最终食物效率**：`1/L × (1+FL) × (1～2)`
- **始终显示需要进食**：`needsFood()` 恒为 `true`，用于配合本 mod 的饥饿值逻辑
- **瞬间进食**：食用任意食物时，进食时间被设为 0
- **自定义 HUD**：显示当前饥饿值等级（鸡腿数量）、饥饿值与饱和度，格式为 `饥饿等级*饥饿值(+饱和度)`

### 食物剑

共 5 个等级，均为「按饥饿值加伤、攻击时消耗饥饿值/饱和度」的剑。

| 剑             | 材质等级   | 额外伤害倍率 |
|----------------|------------|--------------|
| 肥肉剑         | 木         | 1×           |
| 肌肉剑         | 石头       | 2×           |
| 骨剑           | 铁         | 1.5×         |
| 胳膊剑         | 钻石       | 4×           |
| 附魔金胳膊剑   | 下界合金   | 6×           |

- **额外伤害**：基础量 = `L²`，再乘以上表倍率；饥饿等级 ≥ 1 时生效。
- **攻击消耗**：每次命中敌人会消耗 L 饱和度，不足时再扣饥饿值。

### 食物护甲

五套盔甲（肥肉 / 肌肉 / 骨头 / 胳膊 / 金胳膊），每套 4 件（头盔、胸甲、护腿、靴子）。材质等价于：金、链甲、铁、钻石、下界合金。

#### 伤害减免

- 穿戴任意一件「食物盔甲」时，受到伤害会用 **饱和度 + 饥饿值** 抵伤。
- 可减免比例有上限，由当前穿戴的套装档次决定（混搭则按四件平均）：

  | 套装     | 最多减免 |
  |----------|----------|
  | 肥肉套   | 30%      |
  | 肌肉套   | 50%      |
  | 骨头套   | 70%      |
  | 胳膊套   | 85%      |
  | 金胳膊套 | 100%     |

- 当 **饥饿等级 ≥ 2** 时生效；先扣饱和度，再扣饥饿值。

#### 放屁悬浮（护腿）

- 穿着任意一件**食物护腿**时，若 **饥饿等级 ≥ 3**，可在空中长按跳跃实现水平悬浮。
- 每 tick 会消耗少量饱和度（0.01），不会消耗饥饿值。

#### 空中连跳（护腿）

- 穿着任意一件**食物护腿**时，若 **饥饿等级 ≥ 4**，可在空中按跳跃实现连跳。
- 每次会消耗 1 饱和度，不会消耗饥饿值。

#### 空中冲刺（护腿）

- 穿着任意一件**食物护腿**时，若 **饥饿等级 ≥ 5**，可在空中按冲刺键向当前面朝方向冲刺。
- 每次会消耗 1 饱和度，不会消耗饥饿值。

### 剑/护甲制作

- **肥肉材质**：可用任意食物 tag 的材料（`#c:foods`）制作
- **肌肉材质**：在肥肉材质损坏时自动获得
- **骨材质**：使用骨头制作
- **胳膊材质**：使用肥肉 + 肌肉 + 骨头混合制作
- **金胳膊材质**：制作需要附魔金苹果

---

## English

A Minecraft mod that removes the hunger and saturation cap. Swords and armor added by the mod scale with your food level and consume extra saturation/hunger. The mod focuses on number tuning and progression.

### Core mechanics

- **No saturation cap**
- **Food level stage and stage cap**
  - at stage L, max food level is `10^(L+1)`.
- **Tribulation (lightning)** 
  - when food level breaks the current stage cap, tribulation triggers: the player takes lightning damage. After surviving all strikes, food level stage increases by 1.
  - There are L lightning strikes; each strike deals `10^L` damage, with 1 second between strikes.
  - Take it seriously: leaving (server restart, disconnect, invincibility frames, etc.) before all strikes are applied can cause the tribulation to fail; you must trigger it again.
- **Dynamic food efficiency**
  - **Food level FL** (from the food item): `FL = log10(food nutrition)`.
  - **Variety bonus**: eating food gives extra hunger/saturation multiplier ×(1～2) based on the last `L+5` “distinct values” (hunger + saturation combinations).
    - Formula: `(number of distinct values in last L+5 foods / (L+5)) + 1`; rotating through L+5 different foods gives max efficiency.
    - Same hunger + saturation count as one type (e.g. two foods that both give 1 hunger + 1 saturation are one category).
  - **Better food bonus**: multiplier **×(1+FL)** from food level.
  - **Late-game penalty**: multiplier **×1/L** from your current stage L.
  - **Effective efficiency**: `1/L × (1+FL) × (1～2)`.
- **Always “needs food”**: `needsFood()` is always `true` for this mod’s logic.
- **Instant eat**: eating any food has duration set to 0.
- **Custom HUD**: shows food level stage (drumstick icons), hunger and saturation as `stage*hunger(+saturation)`.

### Food Swords

Five tiers; all swords add damage based on food level and consume hunger/saturation on hit.

| Sword           | Material tier | Extra damage multiplier |
|-----------------|----------------|--------------------------|
| Fat Sword       | Wood           | 1×                       |
| Muscle Sword    | Stone          | 2×                       |
| Bone Sword      | Iron           | 1.5×                     |
| Arm Sword       | Diamond        | 4×                       |
| Golden Arm Sword| Netherite      | 6×                       |

- **Extra damage**: base = `L²` × table multiplier; requires food level stage ≥ 1.
- **On hit**: each hit consumes L saturation, then hunger if saturation is insufficient.

### Food Armor

Five sets (Fat / Muscle / Bone / Arm / Golden Arm), four pieces each (helmet, chestplate, leggings, boots). Material tiers match leather, chain, iron, diamond, netherite.

#### Damage reduction

- Wearing any Food Armor piece lets **saturation + hunger** absorb damage.
- Max reduction is capped by the set tier (mixed sets use the average of the four pieces):

  | Set        | Max reduction |
  |------------|----------------|
  | Fat       | 30%            |
  | Muscle    | 50%            |
  | Bone      | 70%            |
  | Arm       | 85%            |
  | Golden Arm| 100%           |

- Active when **food level stage ≥ 2**; saturation is consumed first, then hunger.

#### Hover (leggings)

- With any **Food Leggings**, if **stage ≥ 3**, hold jump in the air to hover.
- Costs a small amount of saturation per tick (0.01); does not consume hunger.

#### Air jump (leggings)

- With any **Food Leggings**, if **stage ≥ 4**, press jump in the air to double jump.
- Costs 1 saturation per jump; does not consume hunger.

#### Air dash (leggings)

- With any **Food Leggings**, if **stage ≥ 5**, press sprint in the air to dash in the direction you’re facing.
- Costs 1 saturation per dash; does not consume hunger.

### Crafting

- **Fat tier**: materials from any food tag (e.g. `#c:foods`).
- **Muscle tier**: obtained automatically when fat gear breaks.
- **Bone tier**: bone.
- **Arm tier**: fat + muscle + bone.
- **Golden Arm tier**: enchanted golden apple.
