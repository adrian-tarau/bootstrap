package net.microfalx.bootstrap.cloud.google;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.web.application.Asset;
import net.microfalx.bootstrap.web.application.AssetBundle;
import net.microfalx.bootstrap.web.application.AssetBundleListener;
import net.microfalx.lang.annotation.Provider;

import java.util.Collection;

import static net.microfalx.lang.StringUtils.isNotEmpty;

@Provider
public class GoogleAssetBundleListener extends ApplicationContextSupport implements AssetBundleListener {

    @Override
    public boolean supports(AssetBundle assetBundle) {
        return "google_cloud".equals(assetBundle.getId());
    }

    @Override
    public void update(AssetBundle assetBundle, Collection<Asset> asserts) {
        GoogleProperties properties = getBean(GoogleProperties.class);
        if (isNotEmpty(properties.getMapApiKey())) {
            initMaps(properties, asserts);
        }
    }

    private void initMaps(GoogleProperties properties, Collection<Asset> asserts) {
        Asset.Builder builder = Asset.uri(Asset.Type.JAVA_SCRIPT, "https://maps.googleapis.com/maps/api/js")
                .async(true).defer(true)
                .parameter("key", properties.getMapApiKey())
                .parameter("callback", "Google.Maps.initialize")
                .parameter("loading", "async")
                .parameter("v", "quarterly")
                .feature(GoogleFeatures.MAP);
        asserts.add(builder.build());

    }
}
