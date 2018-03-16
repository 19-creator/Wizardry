package electroblob.wizardry.entity.construct;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class EntityTornado extends EntityMagicConstruct {

	private double velX, velZ;

	public EntityTornado(World world){
		super(world);
		this.height = 8.0f;
		this.width = 5.0f;
		this.isImmuneToFire = false;
	}

	public EntityTornado(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, double velX,
			double velZ, float damageMultiplier){
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		this.height = 8.0f;
		this.width = 5.0f;
		this.velX = velX;
		this.velZ = velZ;
		this.isImmuneToFire = false;
	}

	public void onUpdate(){

		super.onUpdate();

		if(this.ticksExisted % 120 == 1 && world.isRemote){
			// Repeat is false so that the sound fades out when the tornado does rather than stopping suddenly
			Wizardry.proxy.playMovingSound(this, WizardrySounds.SPELL_LOOP_WIND, 1.0f, 1.0f, false);
		}

		this.move(MoverType.SELF, velX, motionY, velZ);

		BlockPos pos = new BlockPos(this);
		int y = WizardryUtilities.getNearestFloorLevelC(world, pos.up(3), 5);
		pos = new BlockPos(pos.getX(), y, pos.getZ());

		if(this.world.getBlockState(pos).getMaterial() == Material.LAVA){
			// Fire tornado!
			this.setFire(5);
		}

		if(!this.world.isRemote){

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(4.0d, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					double velY = target.motionY;

					double dx = this.posX - target.posX > 0 ? 0.5 - (this.posX - target.posX) / 8
							: -0.5 - (this.posX - target.posX) / 8;
					double dz = this.posZ - target.posZ > 0 ? 0.5 - (this.posZ - target.posZ) / 8
							: -0.5 - (this.posZ - target.posZ) / 8;

					if(this.isBurning()){
						target.setFire(4);
					}

					if(this.getCaster() != null){
						target.attackEntityFrom(
								MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.MAGIC),
								1 * damageMultiplier);
					}else{
						target.attackEntityFrom(DamageSource.MAGIC, 1 * damageMultiplier);
					}

					target.motionX = dx;
					target.motionY = velY + 0.2;
					target.motionZ = dz;

					// Player motion is handled on that player's client so needs packets
					if(target instanceof EntityPlayerMP){
						((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
					}

					// The 'Not Again...' achievement
					if(target instanceof EntityPig && WizardryUtilities.getRider(target) instanceof EntityPlayer){
						WizardryAdvancementTriggers.pig_tornado.triggerFor((EntityPlayer)WizardryUtilities.getRider(target));
					}
				}
			}
		}else{
			for(int i = 1; i < 10; i++){

				double yPos = rand.nextDouble() * 8;

				int blockX = (int)this.posX - 2 + this.rand.nextInt(4);
				int blockZ = (int)this.posZ - 2 + this.rand.nextInt(4);

				BlockPos pos1 = new BlockPos(blockX, this.posY + 3, blockZ);

				int blockY = WizardryUtilities.getNearestFloorLevelC(world, pos1, 5) - 1;

				pos1 = new BlockPos(pos1.getX(), blockY, pos1.getZ());

				IBlockState block = this.world.getBlockState(pos1);

				// If the block it found was air or something it can't pick up, it makes a best guess based on the
				// biome.
				if(!canTornadoPickUpBitsOf(block)){
					block = world.getBiome(pos1).topBlock;
				}

				Wizardry.proxy.spawnTornadoParticle(world, this.posX, this.posY + yPos, this.posZ, this.velX, this.velZ,
						yPos / 3 + 0.5d, 100, block, pos1);
				Wizardry.proxy.spawnTornadoParticle(world, this.posX, this.posY + yPos, this.posZ, this.velX, this.velZ,
						yPos / 3 + 0.5d, 100, block, pos1);

				// Sometimes spawns leaf particles if the block is leaves
				if(block.getMaterial() == Material.LEAVES && this.rand.nextInt(3) == 0){
					double yPos1 = rand.nextDouble() * 8;
					Wizardry.proxy.spawnParticle(WizardryParticleType.LEAF, world,
							this.posX + (rand.nextDouble() * 2 - 1) * (yPos1 / 3 + 0.5d), this.posY + yPos1,
							this.posZ + (rand.nextDouble() * 2 - 1) * (yPos1 / 3 + 0.5d), 0, -0.05, 0,
							40 + rand.nextInt(10));
				}

				// Sometimes spawns snow particles if the block is snow
				if(block.getMaterial() == Material.SNOW
						|| block.getMaterial() == Material.CRAFTED_SNOW && this.rand.nextInt(3) == 0){
					double yPos1 = rand.nextDouble() * 8;
					Wizardry.proxy.spawnParticle(WizardryParticleType.SNOW, world,
							this.posX + (rand.nextDouble() * 2 - 1) * (yPos1 / 3 + 0.5d), this.posY + yPos1,
							this.posZ + (rand.nextDouble() * 2 - 1) * (yPos1 / 3 + 0.5d), 0, -0.02, 0,
							40 + rand.nextInt(10));
				}
			}
		}
	}

	private static boolean canTornadoPickUpBitsOf(IBlockState block){
		Material material = block.getMaterial();
		return material == Material.CRAFTED_SNOW || material == Material.GROUND || material == Material.GRASS
				|| material == Material.LAVA || material == Material.SAND || material == Material.SNOW
				|| material == Material.WATER || material == Material.PLANTS || material == Material.LEAVES
				|| material == Material.VINE;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		velX = nbttagcompound.getDouble("velX");
		velZ = nbttagcompound.getDouble("velZ");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setDouble("velX", velX);
		nbttagcompound.setDouble("velZ", velZ);
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		super.writeSpawnData(data);
		data.writeDouble(velX);
		data.writeDouble(velZ);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		super.readSpawnData(data);
		this.velX = data.readDouble();
		this.velZ = data.readDouble();
	}

}
