import json

from django.contrib import admin
from django.core.serializers.json import DjangoJSONEncoder
from django.http import JsonResponse
from django.urls import path


# Register your models here.


class BaseChartDataAdmin(admin.ModelAdmin):
    change_list_template = 'admin/chart_data/change_list.html'

    chart_label = None
    chart_background_color = 'rgba(220,20,20,0.5)'
    chart_table = True

    # Inject chart data on page load in the ChangeList view
    def changelist_view(self, request, extra_context=None):
        if (self.chart_label is None) and (hasattr(self.model, 'Meta')):
            self.chart_label = 'new ' + self.model.Meta.verbose_name_plural.lower()

        chart_data = self.chart_data(request)
        as_json = json.dumps(list(chart_data), cls=DjangoJSONEncoder)

        extra_context = extra_context or {}
        extra_context['chart_data'] = as_json
        extra_context['chart_label'] = self.chart_label
        extra_context['chart_background_color'] = self.chart_background_color
        extra_context['chart_table'] = self.chart_table
        extra_context['chart_fetch_url'] = request.path + 'chart_data/'

        if not self.chart_table:
            extra_context['title'] = 'Select chart to view details'

        return super().changelist_view(request, extra_context=extra_context)

    def get_urls(self):
        urls = super().get_urls()
        extra_urls = [
            path('chart_data/', self.admin_site.admin_view(self.chart_data_endpoint))
        ]
        # NOTE! The custom URLs have to go before the default URLs, because they default ones match anything.
        return extra_urls + urls

    # JSON endpoint for generating chart data that is used for dynamic loading via JS.
    def chart_data_endpoint(self, request):
        chart_data = self.chart_data(request)
        return JsonResponse(list(chart_data), safe=False)

    def chart_data(self, request):
        """
        Return a queryset for plotting the chart. This queryset must include ``date``
        datetime field for x-axis and ``y`` number field for y-axis.
        """
        return self.get_queryset(request)


class NoWriteAdmin(admin.ModelAdmin):
    def has_add_permission(self, request):
        return False

    def has_delete_permission(self, request, obj=None):
        return False

    def has_change_permission(self, request, obj=None):
        return False
