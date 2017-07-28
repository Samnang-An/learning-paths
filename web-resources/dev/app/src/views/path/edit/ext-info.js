import template from './ext-info.html';

import App from 'application';
import Radio from 'backbone.radio';

import DurationUtil from 'utils/duration-utils';
import ValamisUIControls from 'behaviors/valamis-ui-controls';

import * as enumerations from 'models/enumerations';

const defaultValidPeriod = 'P1Y';
const defaultExpirationPeriod = 'P30D';

const PathExtInfo  = Marionette.View.extend({
    className: 'div-table val-info-table',
    template: template,
    templateContext() {
        let periodTypes = _.map(enumerations.validPeriodTypes, (value, key) => {
            return { value: key, label: App.language[value + 'PeriodLabel'] };
        });

        return {
            periodTypes: periodTypes
        }
    },
    behaviors: [ValamisUIControls],
    ui: {
        validPeriodPermanent: '#permanentPeriod',
        validPeriodNonPermanent: '#nonPermanentPeriod',
        validPeriodValue: '.js-valid-period-value',
        validPeriodTypeSelector: '.js-valid-period-type-selector',
        validPeriodType: '.js-valid-period-type',
        expiringBlock: '.js-expiring-block',
        expiringPeriodValue: '.js-expiring-period-value',
        expiringPeriodType: '.js-expiring-period-type',
        badgesIntegration: '#badgesIntegration',
        badgesDescriptionBlock: '.js-badges-description-block',
        badgesDescription: '.js-badges-description'
    },
    events: {
        'change @ui.validPeriodPermanent': 'onValidPeriodChange',
        'change @ui.validPeriodNonPermanent': 'onValidPeriodChange',
        'change @ui.validPeriodValue input': 'updateValidPeriod',
        'change @ui.validPeriodType': 'updateValidPeriod',
        'change @ui.expiringPeriodValue input': 'updateExpiringPeriod',
        'change @ui.expiringPeriodType': 'updateExpiringPeriod',
        'change @ui.badgesIntegration': 'onBadgesIntegrationChange',
        'change @ui.badgesDescription': 'saveBadgesDescription'
    },
    onRender() {
        let isPermanent = !this.model.get('validPeriod');
        if (isPermanent) {
            this.ui.validPeriodPermanent.prop('checked', true);
        }
        else {
            this.ui.validPeriodNonPermanent.prop('checked', true);
        }

        this.toggleExtraValidInfo(isPermanent);
        this.toggleExtraBadgesInfo(this.model.get('openBadgesEnabled'));

        this.on('valamis:controls:init', () => {
            this.ui.validPeriodValue.valamisPlusMinus({min: 1});
            this.ui.expiringPeriodValue.valamisPlusMinus({min: 1});
            if (!isPermanent) {
                this.showDurations();
            }
        });
    },
    showDurations() {
        let notify = Radio.trigger('notify');

        let validPeriod = DurationUtil.getPeriod(this.model.get('validPeriod'));
        if (_.isEmpty(validPeriod)) {
            notify.trigger('notify', 'warning', 'durationWarningLabel');
            console.log(App.language['durationWarningLabel'] + ' ' + this.model.get('validPeriod'));
        }
        else {
            this.ui.validPeriodValue.valamisPlusMinus('value', validPeriod.value);
            this.ui.validPeriodType.val(validPeriod.type);
        }

        let expiringPeriod = DurationUtil.getPeriod(this.model.get('expiringPeriod'));
        if (_.isEmpty(expiringPeriod)) {
            notify.trigger('notify', 'warning', 'durationWarningLabel');
            console.log(App.language['durationWarningLabel'] + ' ' + this.model.get('expiringPeriod'));
        }
        else {
            this.ui.expiringPeriodValue.valamisPlusMinus('value', expiringPeriod.value);
            this.ui.expiringPeriodType.val(expiringPeriod.type);
        }
    },
    onValidPeriodChange() {
        let isPermanent = this.ui.validPeriodPermanent.prop('checked') === true;

        let newPeriods = (isPermanent)
            ? { validPeriod: null, expiringPeriod: null }
            : { validPeriod: defaultValidPeriod, expiringPeriod: defaultExpirationPeriod };

        this.model.set(newPeriods);
        this.model.trigger('path:save');

        if (!isPermanent) {
            // to unset old data in the fields
            this.render();
        }
        else {
            this.toggleExtraValidInfo(isPermanent);
        }
    },
    toggleExtraValidInfo(isPermanent) {
        this.ui.validPeriodValue.toggleClass('hidden', isPermanent);
        this.ui.validPeriodTypeSelector.toggleClass('hidden', isPermanent);
        this.ui.expiringBlock.toggleClass('hidden', isPermanent);
    },
    updateValidPeriod() {
        let value = this.ui.validPeriodValue.valamisPlusMinus('value');
        let type = this.ui.validPeriodType.val();

        let obj = {};
        obj[type] = value;
        this.model.set('validPeriod', DurationUtil.getIsoDuration(obj));
        this.model.trigger('path:save');
    },
    updateExpiringPeriod() {
        let value = this.ui.expiringPeriodValue.valamisPlusMinus('value');
        let type = this.ui.expiringPeriodType.val();

        let obj = {};
        obj[type] = value;
        this.model.set('expiringPeriod', DurationUtil.getIsoDuration(obj));
        this.model.trigger('path:save');
    },
    onBadgesIntegrationChange() {
        let isEnabled = this.ui.badgesIntegration.prop('checked');

        let newBadges = (isEnabled)
            ? { openBadgesEnabled: true, openBadgesDescription: ''}
            : { openBadgesEnabled: false, openBadgesDescription: null };

        this.model.set(newBadges);
        this.model.trigger('path:save');

        if (isEnabled) {
            // to unset old data in the fields
            this.render();
        }
        else {
            this.toggleExtraBadgesInfo(isEnabled);
        }
    },
    toggleExtraBadgesInfo(isEnabled) {
        this.ui.badgesDescriptionBlock.toggleClass('hidden', !isEnabled);
    },
    saveBadgesDescription() {
        // todo add timeout
        this.model.set('openBadgesDescription', this.ui.badgesDescription.val());
        this.model.trigger('path:save');
    }
});

export default PathExtInfo;