package net.microfalx.bootstrap.cloud.google;

import net.microfalx.bootstrap.core.utils.ApplicationContextSupport;
import net.microfalx.bootstrap.feature.FeatureService;
import net.microfalx.bootstrap.web.application.Asset;
import net.microfalx.bootstrap.web.application.AssetBundle;
import net.microfalx.bootstrap.web.application.AssetBundleListener;
import net.microfalx.lang.annotation.Provider;

import java.util.Collection;

@Provider
public class GoogleAssetBundleListener extends ApplicationContextSupport implements AssetBundleListener {

    @Override
    public boolean supports(AssetBundle assetBundle) {
        return "google_cloud".equals(assetBundle.getId());
    }

    @Override
    public void update(AssetBundle assetBundle, Collection<Asset> asserts) {
        GoogleProperties properties = getBean(GoogleProperties.class);
        if (properties.isMapApiEnabled()) initMaps(properties, asserts);
        if (properties.isGisEnabled()) initGis(properties, asserts);
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

    private void initGis(GoogleProperties properties, Collection<Asset> asserts) {
        FeatureService featureService = getBean(FeatureService.class);
        featureService.setEnabled(GoogleFeatures.GIS_FEATURE, true);
        Asset.Builder builder = Asset.uri(Asset.Type.JAVA_SCRIPT, "https://accounts.google.com/gsi/client")
                .defer(true).feature(GoogleFeatures.GIS)
                .requiresAuthentication(false).onLoad("Google.Gis.initialize('" + properties.getClientId() + "')");
        asserts.add(builder.build());
    }

}
