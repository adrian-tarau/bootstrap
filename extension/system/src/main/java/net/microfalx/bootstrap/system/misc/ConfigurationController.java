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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

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

    @GetMapping(value = "fields/{group}")
    public String getFields(Model model, @PathVariable("group") String groupId) {
        Metadata metadata = configurationService.getMetadata(groupId);
        if (metadata == null) throw new IllegalArgumentException("Invalid group: " + groupId);
        Helper helper = new Helper(metadata);
        updateGroups(model, helper);
        return "misc/configuration :: settings_fields";
    }

    private void updateModel(Model model) {
        updateTitle(model);
        updateGroups(model, null);
    }

    private void updateGroups(Model model, Helper helper) {
        if (helper == null) helper = new Helper();
        model.addAttribute("sections", getSections());
        model.addAttribute("settingsHelper", helper);
        model.addAttribute("activeMetadata", helper.activeMetadata);
    }

    private Collection<Section> getSections() {
        Map<String, Section> sections = new LinkedHashMap<>();
        for (Metadata metadata : configurationService.getRootMetadata().getChildren()) {
            String name = metadata.getSection();
            if (isEmpty(name)) continue;
            String id = StringUtils.toIdentifier(name);
            Section section = sections.computeIfAbsent(id, s -> new Section(id, name));
            section.items.add(metadata);
        }
        return sections.values();
    }

    public static class Helper {

        private Metadata activeMetadata;

        Helper() {
        }

        Helper(Metadata activeMetadata) {
            this.activeMetadata = activeMetadata;
        }

        public String getItemClass(Metadata metadata) {
            if (metadata == activeMetadata) {
                return "active";
            } else {
                return StringUtils.EMPTY_STRING;
            }
        }

        public String getGroupName() {
            return activeMetadata != null ? activeMetadata.getName() : "No Group";
        }

        public Collection<Metadata> getChildren() {
            return activeMetadata != null ? activeMetadata.getChildren() : Collections.emptyList();
        }

        public boolean isTextField(Metadata metadata) {
            return !metadata.isMultiline() && isTextBased(metadata);
        }

        public boolean isTextArea(Metadata metadata) {
            return metadata.isMultiline() && isTextField(metadata);
        }

        public boolean isCheckboxField(Metadata metadata) {
            return metadata.getDataType() == Metadata.DataType.BOOLEAN;
        }

        private boolean isTextBased(Metadata metadata) {
            return metadata.getDataType() != Metadata.DataType.BOOLEAN;
        }

        public String getFieldClass(Metadata metadata) {
            return "";
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
