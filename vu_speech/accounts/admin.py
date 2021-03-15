from django.contrib import admin
from django.contrib.auth import get_user_model
from django.contrib.auth.admin import UserAdmin as DjangoUserAdmin

from .forms import UserCreationForm, UserChangeForm


# Register your models here.


class UserAdmin(DjangoUserAdmin):
    model = get_user_model()
    add_form = UserCreationForm
    form = UserChangeForm


admin.site.register(get_user_model(), UserAdmin)
