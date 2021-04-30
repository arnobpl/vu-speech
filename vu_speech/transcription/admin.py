from admin_numeric_filter.admin import NumericFilterModelAdmin, SliderNumericFilter
from django.contrib import admin
from django.db.models import Count
from django.db.models.functions import TruncDay

from custom_admin.admin import BaseChartDataAdmin, NoWriteAdmin
from . import models


# Register your models here.


@admin.register(models.TranscriptionToken)
class TranscriptionTokenAdmin(admin.ModelAdmin):
    list_display = ('token_value', 'last_used', 'is_active',)
    list_filter = ('last_used', 'is_active',)

    readonly_fields = ('last_used',)

    ordering = ('-last_used',)


@admin.register(models.SpeechUsage)
class SpeechUsageAdmin(BaseChartDataAdmin, NoWriteAdmin):
    # TODO: replace 'usage (sentences)' to 'duration' after getting seconds
    chart_label = 'total speech usage (sentences)'
    chart_table = False

    def chart_data(self, request):
        # TODO: get seconds from total_duration (timedelta type)
        # return (super().chart_data(request)
        #         .annotate(date=TruncDay('speech_datetime'))
        #         .values('date')
        #         .annotate(total_duration=Sum('speech_duration'))
        #         .order_by('-date')
        #         )
        return (super().chart_data(request)
                .annotate(date=TruncDay('speech_datetime'))
                .values('date')
                .annotate(y=Count('id'))
                .order_by('-date')
                )


@admin.register(models.SpeechStability)
class SpeechStabilityAdmin(NumericFilterModelAdmin, NoWriteAdmin):
    list_display = ('phrase', 'average_stability', 'count',)
    list_filter = (('average_stability', SliderNumericFilter), ('count', SliderNumericFilter),)

    ordering = ('average_stability', '-count',)

    search_fields = ('phrase',)
