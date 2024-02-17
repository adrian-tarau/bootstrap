$(document).ready(function(){
    tippy.setDefaultProps({
        delay: [1500, 0],
        arrow: true,
        hideOnClick: true,
        interactive: true,
        theme: 'material',
        animation: 'fade',
        maxWidth: 350,
        allowHTML : true
    });
    tippy('[data-tippy-content]');
});