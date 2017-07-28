import { mixin } from 'core-decorators';

@mixin({
    defaults:{
        'dropdown':'.js-dropdown',
        'plusminus':'.js-plus-minus',
        'digitsOnly':'.js-digits-only',
        'sidebarToggler': '.js-toggle-sidebar',
        'popupPanel': '.js-valamis-popup',
        'valamisRating': '.js-valamis-rating',
        'valamisSearch': '.js-search',
        'valamisTabs': '.js-valamis-tabs'
    }
})
export default class ValamisUIControls extends Marionette.Behavior {
    onRender() {
        this.$(this.options.dropdown).valamisDropDown();
        this.$(this.options.plusminus).valamisPlusMinus();
        this.$(this.options.digitsOnly).valamisDigitsOnly();
        this.$(this.options.sidebarToggler).valamisSidebar();
        this.$(this.options.popupPanel).valamisPopupPanel();
        this.$(this.options.valamisRating).valamisRating();
        this.$(this.options.valamisSearch).valamisSearch();
        this.$(this.options.valamisTabs).valamisTabs();

        this.view.triggerMethod('valamis:controls:init');
    }
}
