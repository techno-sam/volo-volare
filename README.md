<!--suppress HtmlDeprecatedTag, XmlDeprecatedElement, HtmlDeprecatedAttribute -->
<p align="center">
<img alt="mod icon: a colorful glider superimposed on a blue sky with clouds" src="https://raw.githubusercontent.com/techno-sam/volo-volare/refs/heads/fabric-1.21.8/dev/src/main/resources/assets/volare/icon.png" width="128"/>
</p>

<p align="center">
Step into a model glider and soar into a world of wonder.<br/>
</p>

<p align="center">
<a href="https://modfest.net/toybox"><img alt="Made for Modfest: Toybox" src="https://raw.githubusercontent.com/ModFest/art/refs/heads/v2/badge/svg/toybox/compact.svg"></a>
</p>

---

Volo Volare adds a highly-configurable toy glider.
- Give yourself a speed boost by using gunpowder or blaze powder while flying.  
  (item tag: `volare:thrust_source`)
- Extend your flight by flying through thermals produced by campfires, fire, magma, and lava.  
  (block tag: `volare:thermal_source`)
- Configure arbitrary wingtip particles with the `volare:glider/particles` component.  
  `/give @s volare:glider[volare:glider/particles=[{type:"minecraft:electric_spark"}, {type:"minecraft:heart"}]]`
- Give a glider an initial speed boost with the `volare:glider/frozen_motion` component.  
  `/give @s volare:glider[volare:glider/frozen_motion=[0.0f, 0.0f, 0.8f]]`

---

To configure 1st- and 3rd-person roll, edit `config/volare.json5` or use the in-game config screen.

To configure thermals and boost, edit `<world_path>/serverconfig/volare-server.json5` or use the in-game config screen.
Server configs are automatically synced to clients.

---

![a glider flying above the clouds with animals riding on the wings](https://cdn.modrinth.com/data/6vF0WWqw/images/7793e9c1a8a7ad16b59b3ece6710eb8cb6a7b9a2.png)

---

*About the name: The first two principal parts of the latin verb for flight are 'volo volare' ('I fly', 'to fly'), but 'volo' also means 'I want', thus 'volo volare' means 'I want to fly'.*
