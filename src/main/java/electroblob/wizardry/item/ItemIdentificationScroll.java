package electroblob.wizardry.item;

import java.util.List;

import javax.annotation.Nullable;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.event.DiscoverSpellEvent;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemIdentificationScroll extends Item {

	public ItemIdentificationScroll(){
		super();
		setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack){
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(net.minecraft.client.resources.I18n.format("item." + Wizardry.MODID + ":identification_scroll.desc1", "\u00A77"));
		tooltip.add(net.minecraft.client.resources.I18n.format("item." + Wizardry.MODID + ":identification_scroll.desc2", "\u00A77"));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){

		ItemStack stack = player.getHeldItem(hand);

		if(WizardData.get(player) != null){

			WizardData properties = WizardData.get(player);

			for(ItemStack stack1 : WizardryUtilities.getPrioritisedHotbarAndOffhand(player)){

				if(!stack1.isEmpty()){
					Spell spell = Spell.get(stack1.getItemDamage());
					if((stack1.getItem() instanceof ItemSpellBook || stack1.getItem() instanceof ItemScroll)
							&& !properties.hasSpellBeenDiscovered(spell)){

						if(!MinecraftForge.EVENT_BUS.post(new DiscoverSpellEvent(player, spell,
								DiscoverSpellEvent.Source.IDENTIFICATION_SCROLL))){
							// Identification scrolls give the chat readout in creative mode, otherwise it looks like
							// nothing happens!
							properties.discoverSpell(spell);
							WizardryAdvancementTriggers.identify_spell.triggerFor(player);
							player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.25f, 1);
							if(!player.capabilities.isCreativeMode) stack.shrink(1);
							if(!world.isRemote) player.sendMessage(new TextComponentTranslation("spell.discover",
									spell.getNameForTranslationFormatted()));

							return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
						}
					}
				}
			}
			// If it found nothing to identify, it says so!
			if(!world.isRemote) player.sendMessage(
					new TextComponentTranslation("item." + Wizardry.MODID + ":identification_scroll.nothing_to_identify"));
		}

		return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
	}

}
