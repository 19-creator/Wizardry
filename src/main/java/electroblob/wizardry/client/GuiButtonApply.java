package electroblob.wizardry.client;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
class GuiButtonApply extends GuiButton {

	public GuiButtonApply(int id, int x, int y){
		super(id, x, y, 32, 16, I18n.format("container." + Wizardry.MODID + ":arcane_workbench.apply"));
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks){
		
		// Whether the button is highlighted
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

		int k = 36;
		int l = 220;
		int colour = 14737632;

		if(this.enabled){
			if(this.hovered){
				k += this.width * 2;
				colour = 16777120;
			}
		}else{
			k += this.width;
			colour = 10526880;
		}

		WizardryUtilities.drawTexturedRect(this.x, this.y, k, l, this.width, this.height, 256, 256);
		this.drawCenteredString(minecraft.fontRenderer, this.displayString, this.x + this.width / 2,
				this.y + (this.height - 8) / 2, colour);
	}
}