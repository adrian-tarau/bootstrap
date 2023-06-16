$( document ).ready(function() {
    $(".sidebar .dropdown-toggle").on("click", function(event){
        let dropdownParent=$(this.parentElement);
        let dropdownMenu=dropdownParent.find('.dropdown-menu');
        dropdownMenu.toggle();
    });
});