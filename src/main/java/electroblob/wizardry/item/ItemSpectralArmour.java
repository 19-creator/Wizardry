package electroblob.wizardry.item;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSpectralArmour extends ItemArmor implements IConjuredItem {

	public ItemSpectralArmour(ArmorMaterial material, int renderIndex, EntityEquipmentSlot armourType){
		super(material, renderIndex, armourType);
		setCreativeTab(null);
		setMaxDamage(getBaseDuration());
	}

	@Override
	public int getBaseDuration(){
		return 1200;
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack);
	}

	// Overridden to stop the enchantment trick making the name turn blue.
	@Override
	public EnumRarity getRarity(ItemStack stack){
		return EnumRarity.COMMON;
	}

	@Override
	// This method allows the code for the item's timer to be greatly simplified by damaging it directly from
	// onUpdate() and removing the workaround that involved WizardData and all sorts of crazy stuff.
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){

		if(!oldStack.isEmpty() || !newStack.isEmpty()){
			// We only care about the situation where we specifically want the animation NOT to play.
			if(oldStack.getItem() == newStack.getItem() && !slotChanged) return false;
		}

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack){
		int damage = stack.getItemDamage();
		if(damage > stack.getMaxDamage()) player.inventory.clearMatchingItems(this, -1, 1, null);
		stack.setItemDamage(damage + 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack){
		return true;
	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack par2ItemStack){
		return false;
	}

	@Override
	public int getItemEnchantability(){
		return 0;
	}

	// Cannot be dropped
	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player){
		return false;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type){

		if(slot == EntityEquipmentSlot.LEGS) return "wizardry:textures/armour/spectral_armour_legs.png";

		return "wizardry:textures/armour/spectral_armour.png";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack,
			EntityEquipmentSlot armorSlot, ModelBiped _default){
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ZERO
		);
		return super.getArmorModel(entityLiving, itemStack, armorSlot, _default);
	}

}
