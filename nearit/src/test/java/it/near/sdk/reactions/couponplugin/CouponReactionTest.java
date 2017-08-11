package it.near.sdk.reactions.couponplugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import it.near.sdk.reactions.BaseReactionTest;
import it.near.sdk.reactions.contentplugin.model.Image;
import it.near.sdk.reactions.contentplugin.model.ImageSet;
import it.near.sdk.reactions.couponplugin.model.Claim;
import it.near.sdk.reactions.couponplugin.model.Coupon;

import static it.near.sdk.reactions.couponplugin.CouponReaction.CLAIMS_RES;
import static it.near.sdk.reactions.couponplugin.CouponReaction.COUPONS_RES;
import static it.near.sdk.reactions.couponplugin.CouponReaction.IMAGES_RES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CouponReactionTest extends BaseReactionTest<CouponReaction>{

    @Before
    public void setUp() throws Exception {
        reaction = mock(CouponReaction.class);
        setUpMockForRealMethods();
        doCallRealMethod().when(reaction).injectRecipeId(any(Coupon.class), anyString());
        doCallRealMethod().when(reaction).normalizeElement(any(Coupon.class));
    }

    @Test
    public void pluginNameIsReturned() {
        assertThat(reaction.getReactionPluginName(), is(CouponReaction.PLUGIN_NAME));
    }

    @Test
    public void noRefreshUrlProvided() {
        assertThat(reaction.getRefreshUrl(), nullValue());
    }

    @Test
    public void singleReactionUrlShouldBeReturned() {
        String bundleId = "kdfksdfnfdsjf";
        // TODO test this method, it asks for global config internally
    }

    @Test
    public void defaultShowActionShouldBeNull() {
        assertThat(reaction.getDefaultShowAction(), nullValue());
    }

    @Test
    public void injectRecipeShouldDoNothing() {
        Coupon coupon = mock(Coupon.class);
        reaction.injectRecipeId(coupon, "dd");
        verifyZeroInteractions(coupon);
    }

    @Test
    public void normalizeElementShouldNormalizeIcon() throws Image.MissingImageException {
        Coupon coupon = new Coupon();
        Image icon = mock(Image.class);
        ImageSet iconSet = new ImageSet();
        when(icon.toImageSet()).thenReturn(iconSet);
        coupon.icon = icon;

        reaction.normalizeElement(coupon);

        assertThat(coupon.getIconSet(), is(iconSet));
    }

    @Test
    public void shouldReturnModelMap() {
        HashMap<String, Class> modelMap = reaction.getModelHashMap();
        assertThat(modelMap.get(COUPONS_RES), is((Object) Coupon.class));
        assertThat(modelMap.get(CLAIMS_RES), is((Object) Claim.class));
        assertThat(modelMap.get(IMAGES_RES), is((Object) Image.class));
    }
}
