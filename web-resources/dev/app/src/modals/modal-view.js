import _ from "underscore";
import template from "./modal-view.html";

const ModalView = Backbone.Modal.extend({
    template: template,
    className: 'val-modal',
    submitEl: '.js-submit-button',
    cancelEl: '.modal-close',
    clickOutside: function () {
    },
    checkKey: function(e) {
        let keyboard = {
            esc: 27
        };
        if (this.active) {
            switch (e.keyCode) {
                case keyboard.esc:
                    return this.triggerCancel();
            }
        }
    },
    initialize: function (options) {
        this.header = options.header;
        this.isSimple = options.isSimple;
        this.contentView = options.contentView;
        this.customClassName = options.customClassName;
        if (options.template) this.template = options.template;
        if (options.submit) this.submit = options.submit;
        if (options.beforeSubmit) this.beforeSubmit = options.beforeSubmit;
        if (options.beforeCancel) this.beforeCancel = options.beforeCancel;
        if (options.onDestroy) this.onDestroy = options.onDestroy;
    },
    onRender() {
        if (this.customClassName)
            this.$el.addClass(this.customClassName);
        this.$('.modal-content').html(this.contentView.render().el);

        this.$('.js-modal-header').html(this.header);
        if (this.isSimple) {
            this.$('.js-modal-topbar').addClass('hidden');
        }
    },
    onShow() {
        if (this.contentView.onShow && _.isFunction(this.contentView.onShow)) {
            this.contentView.onShow();
        }
    }
});

export default ModalView;
