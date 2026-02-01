package net.microfalx.bootstrap.dos.web;

import net.microfalx.bootstrap.core.utils.CachedAddress;
import net.microfalx.bootstrap.dataset.DataSetService;
import net.microfalx.bootstrap.dataset.State;
import net.microfalx.bootstrap.dataset.annotation.DataSet;
import net.microfalx.bootstrap.dos.DosService;
import net.microfalx.bootstrap.dos.DosUtils;
import net.microfalx.bootstrap.dos.jpa.Rule;
import net.microfalx.bootstrap.help.annotation.Help;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.bootstrap.web.dataset.DataSetController;
import net.microfalx.bootstrap.web.util.JsonFormResponse;
import net.microfalx.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static net.microfalx.lang.StringUtils.isEmpty;
import static net.microfalx.lang.StringUtils.isNotEmpty;

@Controller("DosRuleController")
@RequestMapping("/system/dos/rules")
@DataSet(model = net.microfalx.bootstrap.dos.jpa.Rule.class, timeFilter = false)
@Help("admin/dos/rule")
public class RuleController extends DataSetController<net.microfalx.bootstrap.dos.jpa.Rule, Integer> {

    @Autowired private DosService dosService;

    public RuleController(DataSetService dataSetService) {
        super(dataSetService);
    }

    @Override
    protected void updateFields(net.microfalx.bootstrap.dataset.DataSet<Rule, Field<Rule>, Integer> dataSet, Rule model, State state) {
        super.updateFields(dataSet, model, state);
        if (isEmpty(model.getNaturalId()) && isNotEmpty(model.getAddress()) && model.getType() != null) {
            model.setNaturalId(net.microfalx.bootstrap.dos.Rule.toIdentifier(model.getAddress(), model.getType()));
        }
    }

    @Override
    protected void validate(net.microfalx.bootstrap.dos.jpa.Rule model, State state, JsonFormResponse<?> response) {
        super.validate(model, state, response);
        try {
            if (isNotEmpty(model.getRequestRate())) {
                DosUtils.parseRequestRate(model.getRequestRate());
            }
        } catch (IllegalArgumentException e) {
            response.addError("requestRate", "Invalid request rate");
        }
    }

    @Override
    protected void afterPersist(net.microfalx.bootstrap.dataset.DataSet<net.microfalx.bootstrap.dos.jpa.Rule, Field<net.microfalx.bootstrap.dos.jpa.Rule>,
            Integer> dataSet, net.microfalx.bootstrap.dos.jpa.Rule model, State state) {
        super.afterPersist(dataSet, model, state);
        if (StringUtils.isEmpty(model.getHostname())) {
            CachedAddress address = CachedAddress.get(model.getAddress());
            model.setHostname(address.getHostname());
        }
        dosService.reload();
    }
}
