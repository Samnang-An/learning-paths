
define('jquery', function(){
    if (typeof jQueryValamis === 'undefined') {
        jQueryValamis = jQuery.noConflict();
    }

    return jQueryValamis;
});