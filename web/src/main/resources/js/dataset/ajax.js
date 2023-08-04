$(document).on({
    ajaxStart: function(){
        $("#ajax").show();
    },
    ajaxStop: function(){
        $("#ajax").hide();
    }
});