package com.evacipated.cardcrawl.mod.hubris.patches.falselife;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.hubris.patches.core.AbstractCreature.TempHPField;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;

import java.lang.reflect.Field;

@SpirePatch(
        cls="com.megacrit.cardcrawl.core.AbstractCreature",
        method="renderHealth"
)
public class RenderHealthBar
{
    private static float HEALTH_BAR_HEIGHT = -1;
    private static float HEALTH_BAR_OFFSET_Y = -1;

    @SpireInsertPatch(
            rloc=25,
            localvars={"x", "y"}
    )
    public static void Insert(AbstractCreature __instance, SpriteBatch sb, float x, float y)
    {
        if (HEALTH_BAR_HEIGHT == -1) {
            HEALTH_BAR_HEIGHT = 20.0f * Settings.scale;
            HEALTH_BAR_OFFSET_Y = -28.0f * Settings.scale;
        }

        if (!Gdx.input.isKeyPressed(Input.Keys.H)) {
            if (TempHPField.tempHp.get(__instance) > 0 && __instance.hbAlpha > 0) {
                renderTempHPIconAndValue(__instance, sb, x, y);
            }
        }
    }

    private static <O, T> T getPrivate(Class<O> cls, Object obj, String varName, Class<T> type)
    {
        try {
            Field f = cls.getDeclaredField(varName);
            f.setAccessible(true);
            return (T)f.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <O, T> T getPrivate(Class<O> obj, String varName, Class<T> type)
    {
        try {
            Field f = obj.getDeclaredField(varName);
            f.setAccessible(true);
            return (T)f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void renderTempHPIconAndValue(AbstractCreature creature, SpriteBatch sb, float x, float y)
    {
        sb.setColor(Settings.GOLD_COLOR);
        sb.draw(ImageMaster.BLOCK_ICON,
                x + getPrivate(AbstractCreature.class, "BLOCK_ICON_X", float.class) - 32.0f + creature.hb.width,
                y + getPrivate(AbstractCreature.class, "BLOCK_ICON_Y", float.class) - 32.0f + getPrivate(AbstractCreature.class, creature, "blockOffset", float.class),
                32.0f, 32.0f, 64.0f, 64.0f, Settings.scale, Settings.scale,
                0.0f, 0, 0, 64, 64,
                false, false);
        FontHelper.renderFontCentered(sb, FontHelper.blockInfoFont,
                Integer.toString(TempHPField.tempHp.get(creature)),
                x + getPrivate(AbstractCreature.class, "BLOCK_ICON_X", float.class) + 1.5f * Settings.scale + creature.hb.width,
                y - 16.0f * Settings.scale,
                Settings.CREAM_COLOR,
                1.0f);//TheSpirit.getTemporaryHealth(creature) > 0 ? 5.0f : 1.0f);
    }
}
