let $sortable = $('.sortable');

$sortable.on('click', function () {
    let $this = $(this);
    let field = $this.attr('field_name');
    if (Utils.isDefined(field)) {
        let asc = $this.hasClass('asc');
        let desc = $this.hasClass('desc');
        $sortable.removeClass('asc').removeClass('desc');
        let direction;
        if (desc || (!asc && !desc)) {
            direction = 'asc';
        } else {
            direction = 'desc';
        }
        $this.addClass(direction);
        DataSet.sort(field, direction);
    } else {
        Application.showWarnAlert("Sorting", "Column could not be sorted");
    }
});