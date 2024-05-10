## For Mac

Step 1: In the current directory, create a Python virtual environment by any means, e.g. through typing `python3 -m venv env` in the terminal

Step 2: Activate the virtual environment by typing `source env/bin/activate`

Step 3: Install required packages by typing `pip install -r requirements.txt` in the terminal

Step 4: Enter the `mysite` directory by typing `cd mysite`

Step 5: Run the spider by doing `python3 Spider.py`

Step 6: Host the local server by doing `python3 manage.py runserver`

Step 7: You should now see there is a local link shown in the terminal, e.g. `http://127.0.0.1:8000/`. Click on the link to get to the webpage of the search engine. Then refresh the page.

Step 8: To avoid previous group's cookies are still kept on the webpage, please first press `F12` -> on the DevTools page, go to `Application` -> right click on the local server link under `Cookies` -> press `Clear`.

Step 9: You can do the searching by typing in your query in the search bar, get similar pages by pressing the `get similar pages` button, and review previous query by clicking on the target query under `Search History`

Step 10: After using the search engine, you can terminate the local server by doing `CONTROL + C` in the terminal.

Step 11: Deactivate the virtual environment by typing `deactivate` in the terminal

Note: After page modifications, to update the content in the database, please do Step 5 to Step 9 again.

## For Windows

Step 1: In the current directory, create a Python virtual environment by any means, e.g. through typing `python -m venv env` in the terminal

Step 2: Activate the virtual environment by typing `env\Scripts\activate.bat` in the terminal. If it doesn't work for some reasons, you can also try `comp4321gp14\Scripts\activate`

Step 3: Install required packages by typing `pip install -r requirements.txt` in the terminal

Step 4: Enter the `mysite` directory by typing `cd mysite`

Step 5: Run the spider by doing `python Spider.py`

Step 6: Host the local server by doing `python manage.py runserver`

Step 7: You should now see there is a local link shown in the terminal, e.g. `http://127.0.0.1:8000/`. Click on the link to get to the webpage of the search engine.

Step 8: To avoid previous group's cookies are still kept on the webpage, please first press `F12` -> on the DevTools page, go to `Application` -> right click on the local server link under `Cookies` -> press `Clear`. Then refresh the page.

Step 9: You can do the searching by typing in your query in the search bar, get similar pages by pressing the `get similar pages` button, and review previous query by clicking on the target query under `Search History`

Step 10: After using the search engine, you can terminate the local server by doing `CTRL + C` in the terminal.

Step 11: After finish everything, deactivate the virtual environment by typing `deactivate` in the terminal.

Note: After page modifications, to update the content in the database, please do Step 5 to Step 9 again.
