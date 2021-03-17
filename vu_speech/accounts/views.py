from django.contrib import messages
from django.contrib.auth import update_session_auth_hash
from django.contrib.auth.forms import PasswordChangeForm
from django.contrib.auth.mixins import LoginRequiredMixin
from django.shortcuts import redirect, render
from django.urls import reverse_lazy
from django.views import generic

from . import forms


# Create your views here.


class SignUpView(generic.CreateView):
    form_class = forms.UserCreationForm
    success_url = reverse_lazy('login')
    template_name = 'form.html'
    input_value = 'sign Up'

    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        context['input_value'] = self.input_value
        return context


class ProfileUpdateView(LoginRequiredMixin, generic.UpdateView):
    form_class = forms.UserChangeForm
    template_name = 'form.html'
    input_value = 'update'

    def post(self, request, *args, **kwargs):
        form = forms.UserChangeForm(request.POST, instance=request.user)
        if form.is_valid():
            form.save()
            messages.success(request, 'Your profile was successfully updated!')
            return redirect('/')
        else:
            messages.error(request, 'Please correct the error below.')

    def get(self, request, **kwargs):
        form = forms.UserChangeForm(instance=request.user)
        return render(request, self.template_name, {'form': form, 'input_value': self.input_value})


class PasswordChangeView(LoginRequiredMixin, generic.UpdateView):
    form_class = PasswordChangeForm
    template_name = 'form.html'
    input_value = 'update password'

    def post(self, request, *args, **kwargs):
        form = PasswordChangeForm(request.user, request.POST)
        if form.is_valid():
            user = form.save()
            update_session_auth_hash(request, user)  # Important!
            messages.success(request, 'Your password was successfully updated!')
            return redirect('/')
        else:
            messages.error(request, 'Please correct the error below.')

    def get(self, request, **kwargs):
        form = PasswordChangeForm(request.user)
        return render(request, self.template_name, {'form': form, 'input_value': self.input_value})
