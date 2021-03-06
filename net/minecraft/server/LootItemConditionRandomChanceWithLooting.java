package net.minecraft.server;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;

public class LootItemConditionRandomChanceWithLooting implements LootItemCondition {

    private final float a;
    private final float b;

    public LootItemConditionRandomChanceWithLooting(float f, float f1) {
        this.a = f;
        this.b = f1;
    }

    public boolean a(Random random, LootTableInfo loottableinfo) {
        int i = 0;

        if (loottableinfo.c() instanceof EntityLiving) {
            i = EnchantmentManager.h((EntityLiving) loottableinfo.c());
        }

        return random.nextFloat() < this.a + (float) i * this.b;
    }

    public static class a extends LootItemCondition.a<LootItemConditionRandomChanceWithLooting> {

        protected a() {
            super(new MinecraftKey("random_chance_with_looting"), LootItemConditionRandomChanceWithLooting.class);
        }

        public void a(JsonObject jsonobject, LootItemConditionRandomChanceWithLooting lootitemconditionrandomchancewithlooting, JsonSerializationContext jsonserializationcontext) {
            jsonobject.addProperty("chance", Float.valueOf(lootitemconditionrandomchancewithlooting.a));
            jsonobject.addProperty("looting_multiplier", Float.valueOf(lootitemconditionrandomchancewithlooting.b));
        }

        public LootItemConditionRandomChanceWithLooting a(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
            return new LootItemConditionRandomChanceWithLooting(ChatDeserializer.l(jsonobject, "chance"), ChatDeserializer.l(jsonobject, "looting_multiplier"));
        }

        public LootItemCondition b(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
            return this.a(jsonobject, jsondeserializationcontext);
        }
    }
}
