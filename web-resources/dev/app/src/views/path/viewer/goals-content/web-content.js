import template from './content-in-iframe.html'

import WebContentModel from 'models/web-content'

const WebContent = Marionette.View.extend({
    template: template,
    ui: {
        iframe: 'iframe',
        loading: '.js-loading'
    },
    onRender() {
        this.model = new WebContentModel({id: this.options.webContentId});
        this.ui.iframe.on('load', (e) => {
            if (e.target.src != '') {
                this.ui.loading.addClass('hidden');
                $(e.target).contents().find('head').append($('link.lfr-css-file').first().clone());
                $(e.target).contents().find('html').addClass('aui ltr');

                let that = e.target;
                let timeout = setTimeout(() => {
                    $(that).show();
                }, 100);
            }
        });
        this.ui.iframe.hide();
        this.ui.iframe.attr('src', this.model.getUrl());
    }
});

export default WebContent;