package spinyq.spinytextiles.client.render;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum BodyPart {

	HEAD {
		@Override
		public ModelRenderer getBone(BipedModel<?> model) {
			return model.bipedHead;
		}
	},
	TORSO {
		@Override
		public ModelRenderer getBone(BipedModel<?> model) {
			return model.bipedBody;
		}
	},
	LEFT_ARM {
		@Override
		public ModelRenderer getBone(BipedModel<?> model) {
			return model.bipedLeftArm;
		}
	},
	RIGHT_ARM {
		@Override
		public ModelRenderer getBone(BipedModel<?> model) {
			return model.bipedRightArm;
		}
	},
	LEFT_LEG {
		@Override
		public ModelRenderer getBone(BipedModel<?> model) {
			return model.bipedLeftLeg;
		}
	},
	RIGHT_LEG {
		@Override
		public ModelRenderer getBone(BipedModel<?> model) {
			return model.bipedRightLeg;
		}
	};

	public abstract ModelRenderer getBone(BipedModel<?> model);

}