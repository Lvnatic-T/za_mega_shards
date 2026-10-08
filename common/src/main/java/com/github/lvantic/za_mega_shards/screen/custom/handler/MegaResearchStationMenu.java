package com.github.lvantic.za_mega_shards.screen.custom.handler;

import com.github.lvantic.za_mega_shards.block.ZAMSBlocks;
import com.github.lvantic.za_mega_shards.block.entity.MegaResearchStationBlockEntity;
import com.github.lvantic.za_mega_shards.item.ZAMSItems;
import com.github.lvantic.za_mega_shards.screen.ZAMSMenuTypes;
import com.github.lvantic.za_mega_shards.util.MegaStoneTierHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.github.lvantic.za_mega_shards.advancement.ZAMSAdvancements;
import net.minecraft.server.level.ServerPlayer;


public class MegaResearchStationMenu extends AbstractContainerMenu {

    private static final int INPUT_MENU_SLOT = 0;
    private static final int RESULT_MENU_SLOT = 1;
    private static final int PLAYER_INVENTORY_START = 2;
    private static final int PLAYER_INVENTORY_END = 29;
    private static final int HOTBAR_START = 29;
    private static final int HOTBAR_END = 38;

    private final Container inputContainer;
    private final ResultContainer resultContainer = new ResultContainer();
    private final ContainerLevelAccess access;
    private final boolean clientSide;

    public MegaResearchStationMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(MegaResearchStationBlockEntity.CONTAINER_SIZE), ContainerLevelAccess.NULL);
    }

    public MegaResearchStationMenu(int containerId, Inventory playerInventory, Container inputContainer, ContainerLevelAccess access) {
        super(ZAMSMenuTypes.MEGA_RESEARCH_STATION.get(), containerId);
        checkContainerSize(inputContainer, MegaResearchStationBlockEntity.CONTAINER_SIZE);
        this.inputContainer = inputContainer;
        this.access = access;
        this.clientSide = playerInventory.player.level().isClientSide;

        this.addSlot(new Slot(inputContainer, MegaResearchStationBlockEntity.INPUT_SLOT, 62, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return MegaStoneTierHelper.isMegaStone(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                MegaResearchStationMenu.this.updateResult();
            }
        });

        this.addSlot(new Slot(this.resultContainer, 0, 98, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                MegaResearchStationMenu.this.consumeInput(player);
                super.onTake(player, stack);
            }

        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        this.updateResult();
    }

    private void updateResult() {
        if (this.clientSide) return;

        ItemStack inputStack = this.inputContainer.getItem(MegaResearchStationBlockEntity.INPUT_SLOT);
        if (inputStack.isEmpty() || !MegaStoneTierHelper.isMegaStone(inputStack)) {
            this.resultContainer.setItem(0, ItemStack.EMPTY);
            this.broadcastChanges();
            return;
        }

        int shardAmount = MegaStoneTierHelper.getShardReturn(inputStack);
        if (shardAmount <= 0) {
            this.resultContainer.setItem(0, ItemStack.EMPTY);
            this.broadcastChanges();
            return;
        }

        this.resultContainer.setItem(0, new ItemStack(ZAMSItems.MEGA_SHARD.get(), shardAmount));
        this.broadcastChanges();
    }

    private void consumeInput(Player player) {
        ItemStack inputStack = this.inputContainer.getItem(MegaResearchStationBlockEntity.INPUT_SLOT);
        if (inputStack.isEmpty() || !MegaStoneTierHelper.isMegaStone(inputStack)) return;

        int tier = MegaStoneTierHelper.getTier(inputStack);

        inputStack.shrink(1);
        if (inputStack.isEmpty()) {
            this.inputContainer.setItem(MegaResearchStationBlockEntity.INPUT_SLOT, ItemStack.EMPTY);
        } else {
            this.inputContainer.setItem(MegaResearchStationBlockEntity.INPUT_SLOT, inputStack);
        }
        this.inputContainer.setChanged();

        if (player instanceof ServerPlayer serverPlayer) ZAMSAdvancements.awardDisassembly(serverPlayer, tier);

        this.access.execute((level, pos) -> {
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 2.0F, 0.85F);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 2.0F, 1.15F);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 1.05F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                        8, 0.25D, 0.15D, 0.25D, 0.02D);
            }
        });

        this.updateResult();
    }


    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ZAMSBlocks.MEGA_RESEARCH_STATION.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index == RESULT_MENU_SLOT) {
            if (!this.canFullyMoveToPlayerInventory(stack)) return ItemStack.EMPTY;
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) return ItemStack.EMPTY;
            slot.onTake(player, original);
            return original;
        }

        if (index == INPUT_MENU_SLOT) {
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) return ItemStack.EMPTY;
        } else if (MegaStoneTierHelper.isMegaStone(stack)) {
            if (!this.moveItemStackTo(stack, INPUT_MENU_SLOT, INPUT_MENU_SLOT + 1, false)) return ItemStack.EMPTY;
        } else if (index >= PLAYER_INVENTORY_START && index < PLAYER_INVENTORY_END) {
            if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) return ItemStack.EMPTY;
        } else if (index >= HOTBAR_START && index < HOTBAR_END) {
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    private boolean canFullyMoveToPlayerInventory(ItemStack stack) {
        int remaining = stack.getCount();

        for (int i = PLAYER_INVENTORY_START; i < HOTBAR_END; i++) {
            Slot playerSlot = this.slots.get(i);
            ItemStack existingStack = playerSlot.getItem();

            if (existingStack.isEmpty()) {
                remaining -= Math.min(stack.getMaxStackSize(), remaining);
            } else if (ItemStack.isSameItemSameComponents(existingStack, stack)) {
                int availableSpace = existingStack.getMaxStackSize() - existingStack.getCount();
                if (availableSpace > 0) remaining -= Math.min(availableSpace, remaining);
            }

            if (remaining <= 0) return true;
        }

        return false;
    }

    public Container getInputContainer() {
        return this.inputContainer;
    }

    public ResultContainer getResultContainer() {
        return this.resultContainer;
    }
}