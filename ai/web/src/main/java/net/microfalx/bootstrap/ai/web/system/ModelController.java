package net.microfalx.bootstrap.ai.web.system;

import net.microfalx.bootstrap.ai.api.AiService;
import net.microfalx.bootstrap.ai.web.system.jpa.Model;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.SystemDataSetController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static net.microfalx.lang.StringUtils.toIdentifier;

@Controller("SystemModelController")
@RequestMapping("/system/ai/model")
@DataSet(model = Model.class, timeFilter = false,canAdd = false,canDelete = false)
public class ModelController extends SystemDataSetController<Model,Integer> {

    private final AiService aiService;

    public ModelController(DataSetService dataSetService, AiService aiService) {
        super(dataSetService);
        this.aiService = aiService;
    }

    @Override
    protected void beforePersist(net.microfalx.bootstrap.dataset.DataSet<Model, Field<Model>, Integer> dataSet, Model model, State state) {
        if (model.getNaturalId() == null) model.setNaturalId(toIdentifier(model.getName()));
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<Model, Field<Model>, Integer> dataSet, Model model, State state) {
        super.afterPersist(dataSet, model, state);
        aiService.reload();
    }
}
