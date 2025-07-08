package cn.i7mc.customfurnace.models;

public class FurnaceLevel {
    private final String type;
    private final int level;
    private final int cookingTime;
    private final int vaultCost;
    private final int pointsCost;

    public FurnaceLevel(String type, int level, int cookingTime, int vaultCost, int pointsCost) {
        this.type = type;
        this.level = level;
        this.cookingTime = cookingTime;
        this.vaultCost = vaultCost;
        this.pointsCost = pointsCost;
    }

    @Deprecated
    public FurnaceLevel(String type, int level, int cookingTime, int upgradeCost) {
        this(type, level, cookingTime, upgradeCost, upgradeCost / 10);
    }

    public String getType() {
        return this.type;
    }

    public int getLevel() {
        return this.level;
    }

    public int getCookingTime() {
        return this.cookingTime;
    }

    public int getVaultCost() {
        return this.vaultCost;
    }

    public int getPointsCost() {
        return this.pointsCost;
    }

    @Deprecated
    public int getUpgradeCost() {
        return this.vaultCost;
    }

    public FurnaceLevel getNextLevel(int nextCookingTime, int nextVaultCost, int nextPointsCost) {
        return new FurnaceLevel(this.type, this.level + 1, nextCookingTime, nextVaultCost, nextPointsCost);
    }

    @Deprecated
    public FurnaceLevel getNextLevel(int nextCookingTime, int nextUpgradeCost) {
        return this.getNextLevel(nextCookingTime, nextUpgradeCost, nextUpgradeCost / 10);
    }

    public String toString() {
        return "" + this.level;
    }
}

