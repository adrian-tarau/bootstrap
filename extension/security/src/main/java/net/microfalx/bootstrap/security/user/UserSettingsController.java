package net.microfalx.bootstrap.security.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.LookupProvider;
import net.microfalx.bootstrap.dataset.lookup.TimeZoneLookup;
import net.microfalx.bootstrap.security.SecurityContext;
import net.microfalx.bootstrap.web.controller.PageController;
import net.microfalx.bootstrap.web.util.JsonResponse;
import net.microfalx.lang.CollectionUtils;
import net.microfalx.lang.ObjectUtils;
import net.microfalx.lang.ZoneContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("user/settings")
public class UserSettingsController extends PageController {

    @Autowired private UserService userService;
    @Autowired private DataSetService dataSetService;

    @GetMapping("")
    public String get(Model model) {
        User user = SecurityContext.get().getUser();
        User.Settings settings = user.getSettings();
        model.addAttribute("zones", getTimeZones());
        model.addAttribute("settings", new SettingsForm().setUsername(user.getName())
                .setTimeZone(settings.getTimeZone()).setTheme(settings.getTheme()));
        model.addAttribute("helper", this);
        return "security/user::#user-settings";
    }

    @PostMapping("")
    @ResponseBody
    public JsonResponse<?> save(@ModelAttribute SettingsForm form) {
        User user = SecurityContext.get().getUser();
        User.Settings settings = user.getSettings();
        settings.setTimeZone(form.getTimeZone());
        settings.setTheme(form.getTheme());
        userService.setSetting(UserImpl.USER_SETTINGS, settings);
        return JsonResponse.success().setPayload(settings);
    }

    public boolean isThemeSelected(SettingsForm settings, String theme) {
        return ObjectUtils.equals(settings.getTheme(), theme);
    }

    public boolean isZoneSelected(SettingsForm settings, String zone) {
        return ObjectUtils.equals(settings.getTimeZone(), zone);
    }

    private Collection<TimeZoneLookup> getTimeZones() {
        LookupProvider<TimeZoneLookup, String> lookupProvider = dataSetService.getLookupProvider(TimeZoneLookup.class);
        List<TimeZoneLookup> zones = new ArrayList<>();
        zones.add(new TimeZoneLookup(ZoneContext.UTC_ZONE, "UTC"));
        zones.add(new TimeZoneLookup(ZoneContext.SERVER_ZONE, "Server"));
        zones.add(new TimeZoneLookup(ZoneContext.BROWSER_ZONE, "Browser"));
        zones.add(new TimeZoneLookup("---", "------------"));
        zones.addAll(CollectionUtils.toList(lookupProvider.findAll().iterator()));
        return zones;
    }

    @Getter
    @Setter
    @ToString
    public static class SettingsForm {

        private String username;

        @Size(min = 1, max = 100)
        private String theme;
        @Size(min = 1, max = 100)
        private String timeZone;
    }
}
