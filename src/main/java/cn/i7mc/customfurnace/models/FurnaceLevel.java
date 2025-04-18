package cn.i7mc.customfurnace.models;

/**
 * 熔炉等级属性模型
 */
public class FurnaceLevel {
    private final String type;       // 熔炉类型 (furnace, blast_furnace, smoker)
    private final int level;         // 等级
    private final int cookingTime;   // 烧炼时间(tick)
    private final int vaultCost;     // 金币升级成本
    private final int pointsCost;    // 点券升级成本
    
    /**
     * 新构造函数，支持双支付方式
     */
    public FurnaceLevel(String type, int level, int cookingTime, int vaultCost, int pointsCost) {
        this.type = type;
        this.level = level;
        this.cookingTime = cookingTime;
        this.vaultCost = vaultCost;
        this.pointsCost = pointsCost;
    }
    
    /**
     * 向后兼容的构造函数
     * @deprecated 使用新的构造函数代替
     */
    @Deprecated
    public FurnaceLevel(String type, int level, int cookingTime, int upgradeCost) {
        this(type, level, cookingTime, upgradeCost, upgradeCost / 10); // 默认点券为金币的1/10
    }
    
    public String getType() {
        return type;
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getCookingTime() {
        return cookingTime;
    }
    
    /**
     * 获取金币升级成本
     */
    public int getVaultCost() {
        return vaultCost;
    }
    
    /**
     * 获取点券升级成本
     */
    public int getPointsCost() {
        return pointsCost;
    }
    
    /**
     * 获取升级成本（向后兼容）
     * @deprecated 使用 getVaultCost 或 getPointsCost 代替
     */
    @Deprecated
    public int getUpgradeCost() {
        return vaultCost;
    }
    
    /**
     * 获取下一等级的信息
     */
    public FurnaceLevel getNextLevel(int nextCookingTime, int nextVaultCost, int nextPointsCost) {
        return new FurnaceLevel(type, level + 1, nextCookingTime, nextVaultCost, nextPointsCost);
    }
    
    /**
     * 向后兼容的方法
     * @deprecated 使用 getNextLevel(int, int, int) 代替
     */
    @Deprecated
    public FurnaceLevel getNextLevel(int nextCookingTime, int nextUpgradeCost) {
        return getNextLevel(nextCookingTime, nextUpgradeCost, nextUpgradeCost / 10);
    }
    
    /**
     * 获取显示在物品上的等级标识字符串
     */
    @Override
    public String toString() {
        return level + "";
    }
} 