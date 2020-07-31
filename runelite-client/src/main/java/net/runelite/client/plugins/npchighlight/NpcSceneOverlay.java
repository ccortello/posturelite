/*
 * Copyright (c) 2018, James Swindle <wilingua@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.npchighlight;

import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.opponentinfo.OpponentInfoPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;

public class NpcSceneOverlay extends Overlay {
    // Anything but white text is quite hard to see since it is drawn on
    // a dark background
    private static final Color TEXT_COLOR = Color.WHITE;

    private static final NumberFormat TIME_LEFT_FORMATTER = DecimalFormat.getInstance(Locale.US);

    static {
        ((DecimalFormat) TIME_LEFT_FORMATTER).applyPattern("#0.0");
    }

    private final Client client;
    private final NpcIndicatorsConfig config;
    private final NpcIndicatorsPlugin plugin;
    private static final Actor lastOpponent = OpponentInfoPlugin.lastOpponent;

    @Inject
    NpcSceneOverlay(Client client, NpcIndicatorsConfig config, NpcIndicatorsPlugin plugin) {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (config.showRespawnTimer()) {
            plugin.getDeadNpcsToDisplay().forEach((id, npc) -> renderNpcRespawn(npc, graphics));
        }

        for (NPC npc : plugin.getHighlightedNpcs()) {
            Color color = (npc.getInteracting() == client.getLocalPlayer() || lastOpponent == npc) ? config.getAggressiveHighlightColor() : config.getHighlightColor();
            renderNpcOverlay(graphics, npc, color);
        }

        return null;
    }

    private void renderNpcRespawn(final MemorizedNpc npc, final Graphics2D graphics) {
        if (npc.getPossibleRespawnLocations().isEmpty()) {
            return;
        }

        final WorldPoint respawnLocation = npc.getPossibleRespawnLocations().get(0);
        final LocalPoint lp = LocalPoint.fromWorld(client, respawnLocation.getX(), respawnLocation.getY());

        if (lp == null) {
            return;
        }

        final Color color = config.getHighlightColor();

        final LocalPoint centerLp = new LocalPoint(
                lp.getX() + Perspective.LOCAL_TILE_SIZE * (npc.getNpcSize() - 1) / 2,
                lp.getY() + Perspective.LOCAL_TILE_SIZE * (npc.getNpcSize() - 1) / 2);

        final Polygon poly = Perspective.getCanvasTileAreaPoly(client, centerLp, npc.getNpcSize());

        if (poly != null) {
            OverlayUtil.renderPolygon(graphics, poly, color);
        }

        final Instant now = Instant.now();
        final double baseTick =
				((npc.getDiedOnTick() + npc.getRespawnTime()) - client.getTickCount()) * (Constants.GAME_TICK_LENGTH / 1000.0);
        final double sinceLast = (now.toEpochMilli() - plugin.getLastTickUpdate().toEpochMilli()) / 1000.0;
        final double timeLeft = Math.max(0.0, baseTick - sinceLast);
        final String timeLeftStr = TIME_LEFT_FORMATTER.format(timeLeft);

        final int textWidth = graphics.getFontMetrics().stringWidth(timeLeftStr);
        final int textHeight = graphics.getFontMetrics().getAscent();

        final Point canvasPoint = Perspective
                .localToCanvas(client, centerLp, respawnLocation.getPlane());

        if (canvasPoint != null) {
            final Point canvasCenterPoint = new Point(
                    canvasPoint.getX() - textWidth / 2,
                    canvasPoint.getY() + textHeight / 2);

            OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, timeLeftStr, TEXT_COLOR);
        }
    }

    private void renderNpcOverlay(Graphics2D graphics, NPC actor, Color color) {
        NPCComposition npcComposition = actor.getTransformedComposition();
        if (npcComposition == null || !npcComposition.isInteractible()
                || (actor.isDead() && config.ignoreDeadNpcs())) {
            return;
        }

        if (config.highlightHull()) {
            Shape objectClickbox = actor.getConvexHull();
            renderPoly(graphics, color, objectClickbox);
        }

        if (config.highlightTile()) {
            int size = npcComposition.getSize();
            LocalPoint lp = actor.getLocalLocation();
            Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);

            renderPoly(graphics, color, tilePoly);
        }

        if (config.highlightTrueTile()) {
            int size = npcComposition.getSize();
            LocalPoint p = LocalPoint.fromWorld(client, actor.getWorldLocation());

            if (p == null)
                return;
            int x = p.getX() + Perspective.LOCAL_TILE_SIZE * (size - 1) / 2;
            int y = p.getY() + Perspective.LOCAL_TILE_SIZE * (size - 1) / 2;

            Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, new LocalPoint(x, y), size);

            renderPoly(graphics, color, tilePoly);
        }

        if (config.highlightCenterTile()) {
			int size = npcComposition.getSize();
			LocalPoint lp = actor.getLocalLocation();
			Polygon southWestTilePoly = Perspective.getCanvasTilePoly(client, lp);

			renderPoly(graphics, color, southWestTilePoly);
        }

        if (config.highlightTrueCenterTile()) {
            int size = npcComposition.getSize();

            LocalPoint p = LocalPoint.fromWorld(client, actor.getWorldLocation());

            if (p == null)
                return;
            int x = p.getX() + Perspective.LOCAL_TILE_SIZE * (size - 1) / 2;
            int y = p.getY() + Perspective.LOCAL_TILE_SIZE * (size - 1) / 2;

            Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, new LocalPoint(x, y), 1);

            if (!config.highlightTrueTile() || size != 1)
                renderPoly(graphics, color, tilePoly);
        }

        if (config.highlightSouthWestTile()) {
            int size = npcComposition.getSize();
            LocalPoint lp = actor.getLocalLocation();

            int x = lp.getX() - ((size - 1) * Perspective.LOCAL_TILE_SIZE / 2);
            int y = lp.getY() - ((size - 1) * Perspective.LOCAL_TILE_SIZE / 2);

            Polygon southWestTilePoly = Perspective.getCanvasTilePoly(client, new LocalPoint(x, y));

            renderPoly(graphics, color, southWestTilePoly);
        }

        if (config.highlightTrueSouthWestTile()) {
            LocalPoint p = LocalPoint.fromWorld(client, actor.getWorldLocation());

            if (p == null)
                return;

            renderPoly(graphics, color, Perspective.getCanvasTilePoly(client, p));
        }

        if (config.drawNames() && actor.getName() != null) {
            String npcName = Text.removeTags(actor.getName());
            Point textLocation = actor.getCanvasTextLocation(graphics, npcName, actor.getLogicalHeight() + 40);

            if (textLocation != null) {
                OverlayUtil.renderTextLocation(graphics, textLocation, npcName, color);
            }
        }
    }

    private void renderPoly(Graphics2D graphics, Color color, Shape polygon) {
        if (polygon != null) {
            graphics.setColor(color);
            graphics.setStroke(new BasicStroke(2));
            graphics.draw(polygon);
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
            graphics.fill(polygon);
        }
    }
}
