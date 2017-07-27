import { mixin } from "core-decorators";
import ModalView from "modals/modal-view";

// do not remove even it is shown as unused
import Radio from 'backbone.radio';

//TODO add init method that will use one (first) modal region if this service will used by several applications

@mixin({
    channelName: 'modal',
    radioEvents: {
        'open': 'open',
        'open:as:modal': 'openAsModal',
        'close': 'close',
        'submit': 'submit'
    }
})
class ModalService extends Marionette.Object  {

    constructor(options) {
        super(arguments);
        options || (options = {});
        if(!options.region) throw Error('Modal region is not defined');
        this.modalRegion = options.region;
    }

    open(view) {
        this.modalRegion.show(view);
    }
    openAsModal(view, options){

        const params = Object.assign({}, options, {contentView: view});

        const modalView = new ModalView(params);

        this.modalRegion.show(modalView);
    }

    close(view) {
        this.modalRegion.destroy(view);
    }

    submit(view, options) {
    }
}

export default ModalService;