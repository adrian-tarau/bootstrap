let $sortable = $('.sortable');

$sortable.on('click', function () {
    let $this = $(this);
    let field = $this.attr('field');
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
    sortDataSet(field, direction);
});