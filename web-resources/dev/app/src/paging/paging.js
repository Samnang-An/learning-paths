import paginatorTemplate from './paginator.html';
import showingTemplate from './showing.html';
import showMoreTemplate from './show-more.html'

export const PageModel = Backbone.Model.extend({
    defaults: {
        itemsOnPage: 40,
        startElementNumber: 0,
        endElementNumber: 0,
        navbuttons: [],
        rightDots: false,
        leftDots: false,
        currentPage: 1,
        totalPages: 0,
        firstPage: {page: 0, isActive: false},
        lastPage: {page: 0, isActive: false},
        showPages: 5,
        isPrevVisible: false,
        isNextVisible: false,
        allowShowAll: false,
        isShowingAll: false,
        isLazy: false
    },
    initialize() {
        this.countStartEndElements();
        this.on('change', this.countStartEndElements);
    },
    hasMore() {
        return this.get('totalElements') > this.get('currentPage') * this.get('itemsOnPage');
    },
    getShowingCount() {
        let requested = (this.get('isLazy'))
            ? this.get('currentPage') * this.get('itemsOnPage')
            : this.get('itemsOnPage');
        return Math.min(requested, this.get('totalElements'));
    },
    countStartEndElements() {
        const currentPage = this.get('currentPage');
        const itemsOnPage = this.get('itemsOnPage');
        const totalElements = this.get('totalElements');
        const isShowingAll = this.get('isShowingAll');
        const isLazy = this.get('isLazy');
        let showPages = this.get('showPages');

        let totalPages = Math.floor(totalElements / itemsOnPage);
        if (totalElements % itemsOnPage !== 0) totalPages++;

        if (totalPages <= showPages) showPages = totalPages;

        let showPagesHalf = Math.floor(showPages / 2);

        let subStartPage = currentPage - showPagesHalf;

        if (showPages % 2 == 0) subStartPage++;

        let subEndPage = currentPage + showPagesHalf;

        if (subStartPage <= 2) {
            subEndPage = showPages;
            if (subStartPage == 2 && showPages < 5) subEndPage++;
            subStartPage = 1;
        }

        if (subEndPage > totalPages - 1) {
            subStartPage = totalPages - showPages + 1;
            if (subEndPage == totalPages - 1 && showPages < 5) {
                subStartPage--;
            }

            subEndPage = totalPages;
        }

        let leftDots = subStartPage > 2;
        let rightDots = subEndPage < totalPages - 1;

        let navbuttons = [];
        for (let page = subStartPage; page <= subEndPage; page++) {
            navbuttons[page - subStartPage] = {
                page: page,
                isActive: page == currentPage
            };
        }

        let endElement = isShowingAll ? totalElements : Math.min(currentPage * itemsOnPage, totalElements);
        let startElement = isLazy ? 1 : (currentPage - 1) * itemsOnPage + 1;
        this.set(
            {
                startElementNumber: Math.min(startElement, totalElements),
                endElementNumber: endElement,
                firstPage: {page: 1, isActive: currentPage == 1},
                lastPage: {page: totalPages, isActive: currentPage == totalPages},
                navbuttons: navbuttons,
                leftDots: leftDots,
                rightDots: rightDots,
                isPrevVisible: currentPage > 1,
                isNextVisible: currentPage < totalPages
            }, {silent: true});
    }
});

export const ValamisPaginator = Backbone.Marionette.View.extend({
    template: paginatorTemplate,
    ui: {
        'paginationGroup': '.pagination-group'
    },
    events: {
        'click .js-paginator-previous-page': 'previous',
        'click .js-paginator-next-page': 'next',
        'click .js-paginator-change-page': 'onPageChanged'
    },
    initialize(options) {
        options = options || {};
        this.options = {};

        if (options.model === undefined) this.model = new PageModel();

        this.options.language = options.language || [];
        this.topEdgeParentView = options.topEdgeParentView;
        this.topEdgeSelector = options.topEdgeSelector || '';
        this.topEdgeOffset = _(options.topEdgeOffset).isNumber() ? options.topEdgeOffset : 10;

        this.model.on('change', this.render);
        this.model.on('showAll', function () {
            this.ui.paginationGroup.addClass('hidden');
        });
    },

    onRender() {
        this.ui.paginationGroup
            .toggleClass('hidden', this.model.get('totalElements') <= this.model.get('itemsOnPage'));
    },

    updateItems(total) {
        this.model.set({totalElements: total});
    },

    currentPage() {
        return this.model.get('currentPage');
    },

    itemsOnPage () {
        return this.model.get('itemsOnPage');
    },

    previous() {
        let current = parseInt(this.model.get('currentPage'));
        if (current == 1) return;
        this.updatePage(current - 1);
    },

    next () {
        let current = parseInt(this.model.get('currentPage'));
        if (current >= parseInt(this.model.get('totalElements')) / parseInt(this.model.get('itemsOnPage'))) return;
        this.updatePage(current + 1);
    },

    setItemsPerPage (count) {
        this.model.set({
            itemsOnPage: count
        });
    },

    onPageChanged(event){
        var page = jQuery(event.target).attr('data-id');
        this.updatePage(page);
    },

    updatePage (current) {
        this.model.set({'currentPage': current});

        this.trigger('pageChanged', this);
        this.model.trigger('pageChanged', this);

        if (this.topEdgeParentView != undefined) {
            let topEdgeElement = (this.topEdgeParentView).$(this.topEdgeSelector);

            if (topEdgeElement.length) {
                this.scrollToDataTop(topEdgeElement);
            }
        }
    },

    scrollToDataTop (topEdgeElement) {

        const modalContainer = topEdgeElement.closest('.val-modal');

        if (modalContainer.length > 0) {

            let modalContent = modalContainer.children('.bbm-modal');
            let relativeOffset = topEdgeElement.offset().top - modalContent.offset().top;

            modalContainer.animate({
                scrollTop: relativeOffset + parseInt(modalContainer.css('padding-top')) - this.topEdgeOffset
            }, 500);

        } else {

            var dockbarHeight = $('header#banner .dockbar').outerHeight() || 0; // for LR6
            var controlMenuHeight = $('#ControlMenu').outerHeight() || 0; // for LR7

            $('html, body').animate({
                scrollTop: topEdgeElement.offset().top - (dockbarHeight + controlMenuHeight + this.topEdgeOffset)
            }, 500);

        }
    }
});

export const ValamisPaginatorShowing = Backbone.Marionette.View.extend({
    template: showingTemplate,
    className: 'paging-showing-label',
    events: {
        'click .js-show-all-items': 'showAllItems',
        'click .js-show-page-items': 'showPageItems'
    },
    initialize(options) {
        var settings = options || {};
        this.options = {};
        this.options.language = settings.language || [];
        this.model.on('change', this.render);
    },
    showAllItems() {
        this.model.set({'isShowingAll': true});
        this.model.trigger('showAll', true);
    },
    showPageItems() {
        this.model.set({'isShowingAll': false});
        this.model.trigger('showAll', false);
    },
    onRender() {
        this.$('.js-show-all-items')
            .toggleClass('hidden', this.model.get('totalElements') <= this.model.get('itemsOnPage'));
    }
});

export const ValamisPaginatorShowMore = Backbone.Marionette.View.extend({
    template: showMoreTemplate,
    className: 'paging-show-more hidden',
    events: {
        'click .js-show-more': 'showMore'
    },
    initialize: function() {
        this.model.on('change', () => {
            this.$el.toggleClass('hidden', !this.model.hasMore());
        }, this);
    },
    showMore: function () {
        var current = this.model.get('currentPage') + 1;
        this.model.set({'currentPage': current});
        this.trigger('pageChanged', this);
        this.model.trigger('pageChanged', this);
    }
});
