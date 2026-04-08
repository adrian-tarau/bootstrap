package net.microfalx.bootstrap.system.misc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.microfalx.bootstrap.configuration.ConfigurationService;
import net.microfalx.bootstrap.configuration.Metadata;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.web.controller.SystemPageController;
import net.microfalx.lang.StringUtils;
import net.microfalx.lang.annotation.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.microfalx.lang.StringUtils.isEmpty;

@Controller("ConfigurationAdminController")
@RequestMapping(value = "/system/settings")
@Help("admin/settingsn")
@Name("Settings")
public class ConfigurationController extends SystemPageController {

    @Autowired
    private ConfigurationService configurationService;

    @GetMapping(value = "")
    public String get(Model model) {
        updateModel(model);
        return "misc/configuration";
    }

    private void updateModel(Model model) {
        updateTitle(model);
        updateGroups(model);
    }

    private void updateGroups(Model model) {
        Helper helper = new Helper();
        model.addAttribute("sections", getSections(helper));
        model.addAttribute("settingsHelper", helper);
        model.addAttribute("activeMetadata", helper.activeMetadata);
    }

    private Collection<Section> getSections(Helper helper) {
        Map<String, Section> sections = new LinkedHashMap<>();
        for (Metadata metadata : configurationService.getRootMetadata().getChildren()) {
            String name = metadata.getSection();
            if (isEmpty(name)) continue;
            String id = StringUtils.toIdentifier(name);
            Section section = sections.computeIfAbsent(id, s -> new Section(id, name));
            section.items.add(metadata);
            if (helper.activeMetadata == null) helper.activeMetadata = metadata;
        }
        return sections.values();
    }

    public static class Helper {

        private Metadata activeMetadata;

        public String getItemClass(Metadata metadata) {
            if (metadata == activeMetadata) {
                return "active";
            } else {
                return StringUtils.EMPTY_STRING;
            }
        }
    }


    @RequiredArgsConstructor
    @Getter
    public static class Section {

        private final String id;
        private final String name;
        private final Collection<Metadata> items = new ArrayList<>();

    }
}
