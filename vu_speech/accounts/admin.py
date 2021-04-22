from django.contrib import admin
from django.contrib.auth import get_user_model
from django.contrib.auth.admin import UserAdmin as DjangoUserAdmin
from django.db.models import Count
from django.db.models.functions import TruncDay
from rest_framework.authtoken.models import Token, TokenProxy

from custom_admin.admin import BaseChartDataAdmin
from .forms import forms


# Register your models here.


@admin.register(get_user_model())
class UserAdmin(DjangoUserAdmin, BaseChartDataAdmin):
    list_display = ('username', 'email', 'first_name', 'last_name', 'last_login', 'date_joined', 'is_staff',)
    list_filter = ('last_login', 'date_joined', 'is_staff',)

    add_form = forms.UserCreationForm
    form = forms.UserChangeForm

    date_hierarchy = 'date_joined'

    def chart_data(self, request):
        return (super().chart_data(request)
                .annotate(date=TruncDay('date_joined'))
                .values('date')
                .annotate(y=Count('id'))
                .order_by('-date')
                )


admin.site.unregister(TokenProxy)


@admin.register(Token)
class TokenAdmin(admin.ModelAdmin):
    list_display = ('key', 'user', 'created',)
    list_filter = ('created',)

    date_hierarchy = 'created'
