from django import forms

class UserQuery(forms.Form):
    query = forms.CharField(label="", widget=forms.TextInput(attrs={'class': 'searchBar'}))
