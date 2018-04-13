# In App Events
 
You can define new triggers using NearIT In-App Events.

An in-app event can trigger recipes based on all type of events, like user registration, item added to the chart, NFC tag detected, etc....

The setup is super-easy. 

- Call this method inside your App:
<div class="code-java">
NearItManager.getInstance().triggerInAppEvent("my_awesome_event");
</div>
<div class="code-kotlin">
NearItManager.getInstance().triggerInAppEvent("my_awesome_event")
</div>


- Open NearIT and setup your trigger inside the **Settings> In-App-Events Mapping** section.