package com.evacipated.cardcrawl.mod.hubris.patches;

import com.evacipated.cardcrawl.mod.hubris.relics.Teleporter;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.AbstractRelic;

public class TeleporterPatch
{
    public static boolean isDirectlyConnectedTo(MapRoomNode start, MapRoomNode end)
    {
        for (MapEdge edge : start.getEdges()) {
            if (end.x == edge.dstX && end.y == edge.dstY) {
                return true;
            }
        }
        return false;
    }

    @SpirePatch(
            cls="com.megacrit.cardcrawl.map.MapRoomNode",
            method="isConnectedTo"
    )
    public static class IsConnectedTo
    {
        private static int depth = 0;

        public static boolean Postfix(boolean __result, MapRoomNode __instance, MapRoomNode node)
        {
            ++depth;
            AbstractRelic teleporter = AbstractDungeon.player.getRelic(Teleporter.ID);
            if (!__result && depth < 2 && teleporter != null && teleporter.counter > 0) {
                for (MapEdge edge : __instance.getEdges()) {
                    MapRoomNode nextNode = getNode(edge.dstX, edge.dstY);
                    if (nextNode != null && nextNode.isConnectedTo(node)) {
                        --depth;
                        // Pulse the relic if this node is hovered
                        if (node.hb.hovered) {
                            teleporter.energyBased = true;
                        }
                        return true;
                    }
                }
            }
            --depth;
            return __result;
        }

        private static MapRoomNode getNode(int x, int y)
        {
            try {
                return CardCrawlGame.dungeon.getMap().get(y).get(x);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }

        static int getNodeDistance(MapRoomNode start, MapRoomNode end)
        {
            return getNodeDistance(start, end, 1);
        }

        private static int getNodeDistance(MapRoomNode start, MapRoomNode end, int depth)
        {
            if (start == null) {
                return -1;
            }

            for (MapEdge edge : start.getEdges()) {
                MapRoomNode nextNode = getNode(edge.dstX, edge.dstY);
                if (nextNode != null && nextNode.equals(end)) {
                    return depth;
                }
                int dist = getNodeDistance(nextNode, end, depth+1);
                if (dist != -1) {
                    return dist;
                }
            }
            return -1;
        }
    }

    @SpirePatch(
            cls="com.megacrit.cardcrawl.map.MapRoomNode",
            method="playNodeSelectedSound"
    )
    public static class NodeSelected
    {
        public static void Postfix(MapRoomNode __instance)
        {
            if (Settings.isDebug) {
                return;
            }

            int distance = IsConnectedTo.getNodeDistance(AbstractDungeon.getCurrMapNode(), __instance);
            if (distance > 1) {
                AbstractRelic teleporter = AbstractDungeon.player.getRelic(Teleporter.ID);
                if (teleporter != null) {
                    teleporter.setCounter(teleporter.counter - 1);
                    teleporter.flash();
                }
            }
        }
    }
}
