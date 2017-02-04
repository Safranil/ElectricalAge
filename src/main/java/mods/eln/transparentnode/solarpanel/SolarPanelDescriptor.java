package mods.eln.transparentnode.solarpanel;

import mods.eln.cable.CableRenderDescriptor;
import mods.eln.ghost.GhostGroup;
import mods.eln.misc.*;
import mods.eln.misc.Obj3D.Obj3DPart;
import mods.eln.node.transparent.TransparentNodeDescriptor;
import mods.eln.node.transparent.TransparentNodeEntity;
import mods.eln.sim.ElectricalLoad;
import mods.eln.wiki.Data;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static mods.eln.i18n.I18N.tr;

public class SolarPanelDescriptor extends TransparentNodeDescriptor {

    final Coordonate groundCoordinate;
    boolean basicModel;
    private Obj3D obj;
    private Obj3DPart main;
    private Obj3DPart panel;

    public SolarPanelDescriptor(
        String name,
        Obj3D obj, CableRenderDescriptor cableRender,
        GhostGroup ghostGroup, int solarOffsetX, int solarOffsetY, int solarOffsetZ,
        Coordonate groundCoordinate, double electricalUmax, double electricalPmax,
        double electricalDropFactor,
        double alphaMin, double alphaMax

    ) {
        super(name, SolarPanelElement.class, SolarPanelRender.class);
        this.groundCoordinate = groundCoordinate;
        this.ghostGroup = ghostGroup;

        electricalRs = electricalUmax * electricalUmax * electricalDropFactor
            / electricalPmax / 2.0;
        this.electricalPmax = electricalPmax;
        this.solarOffsetX = solarOffsetX;
        this.solarOffsetY = solarOffsetY;
        this.solarOffsetZ = solarOffsetZ;
        this.alphaMax = alphaMax;
        this.alphaMin = alphaMin;
        basicModel = true;
        this.electricalUmax = electricalUmax;

        this.obj = obj;
        if (obj != null) {
            main = obj.getPart("main");
            panel = obj.getPart("panel");
        }

        this.cableRender = cableRender;

        canRotate = alphaMax != alphaMin;

        voltageLevelColor = VoltageLevelColor.Neutral;
    }


    public void setParent(net.minecraft.item.Item item, int damage) {
        super.setParent(item, damage);
        Data.addEnergy(newItemStack());
    }

    @Override
    public boolean use2DIcon() {
        return true;
    }

    CableRenderDescriptor cableRender;
    double electricalUmax;
    double electricalPmax;

    int solarOffsetX, solarOffsetY, solarOffsetZ;
    double alphaMin, alphaMax;
    //double efficiency;
    double electricalRs;

    boolean canRotate;

    public void applyTo(ElectricalLoad load) {
        load.setRs(electricalRs);
    }


    public double alphaTrunk(double alpha) {
        if (alpha > alphaMax) return alphaMax;
        if (alpha < alphaMin) return alphaMin;
        return alpha;
    }


    void draw(float alpha, Direction front) {
        if (panel != null) {
            GL11.glPushMatrix();
            panel.draw(alpha, 0f, 0f, 1f);
            GL11.glPopMatrix();
        }
        front.glRotateZnRef();
        if (main != null) main.draw();
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
                                         ItemRendererHelper helper) {

        return type != ItemRenderType.INVENTORY;
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        if (type == ItemRenderType.INVENTORY) {
            super.renderItem(type, item, data);
        } else {
            draw((float) alphaMin, Direction.XN);
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4) {
        super.addInformation(itemStack, entityPlayer, list, par4);

        list.add(tr("Produces power from solar radiation."));
        list.add("  " + tr("Max. voltage: %1$V", Utils.plotValue(electricalUmax)));
        list.add("  " + tr("Max. power: %1$W", Utils.plotValue(electricalPmax)));
        if (canRotate) list.add(tr("Can be geared towards the sun."));
    }

    @Override
    public void addCollisionBoxesToList(AxisAlignedBB par5AxisAlignedBB, List list, TransparentNodeEntity entity) {
        if (canRotate) {
            super.addCollisionBoxesToList(par5AxisAlignedBB, list, entity);
            return;
        }
        AxisAlignedBB bb = Blocks.stone.getCollisionBoundingBoxFromPool(entity.getWorldObj(), entity.xCoord, entity.yCoord, entity.zCoord);
        bb.maxY -= 0.5;
        if (par5AxisAlignedBB.intersectsWith(bb)) list.add(bb);
    }
}
