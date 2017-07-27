import _ from 'lodash';

export default class RendererOverride {
    static init(options) {
        options = options || {};
        // Render a template with data by passing in the template
        // selector and the data to render.
        // Render a template with data. The `template` parameter is
        // passed to the `TemplateCache` object to retrieve the
        // template function. Override this method to provide your own
        // custom rendering and template handling for all of Marionette.
        Marionette.Renderer.render = (template, data, view) =>  {
                _.extend(data, {language: options.language});
                _.extend(data, {permissions: options.permissions});

                if (!template) {
                    throw new Marionette.Error({
                        name: 'TemplateNotFoundError',
                        message: 'Cannot render the template since its false, null or undefined.'
                    });
                }

                var templateFunc = _.isFunction(template) ? template : Marionette.TemplateCache.get(template);
                return templateFunc(data);
            }
        };
}
