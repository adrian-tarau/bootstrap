package net.microfalx.bootstrap.dataset.formatter;

import com.google.common.primitives.Doubles;
import net.microfalx.bootstrap.dataset.annotation.Formattable;
import net.microfalx.bootstrap.model.Field;
import net.microfalx.lang.AnnotationUtils;

@Formattable
public class NumberFormatter<M, F extends Field<M>, T> extends AbstractFormatter<M, F, T> {

    @Override
    protected String doFormat(T value, F field, M model) {
        Formattable formattableAnnot = field.findAnnotation(Formattable.class);
        if (formattableAnnot == null) formattableAnnot = AnnotationUtils.getAnnotation(this, Formattable.class);
        return FormatterUtils.basicFormatting(value, formattableAnnot);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T parse(String text, F field) {
        Double value = Doubles.tryParse(text);
        return (T) Field.from(value, field.getDataClass());
    }
}
