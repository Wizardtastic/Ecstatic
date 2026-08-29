# **Ecstatic**

![](https://media.forgecdn.net/attachments/description/1602940/description_49efa71f-514e-4676-8e70-eaf97a7dd002.png)

Ecstatic is an LOD mod designed to be as efficient and as close to vanilla performance as possible, while allowing you to hit render distances of over 256 chunks

 

CC BY-NC SA 4.0 Liscence, source at [https://github.com/Wizardtastic/Ecstatic](https://github.com/Wizardtastic/Ecstatic)

 

***

Small warning! The mod has some weird bugs at the moment. LODs might not look perfect or take a while to generate

***

## **How does Ecstatic work?**

Instead of actually loading faraway chunks like Distant Horizons (Which is incredibly CPU and RAM heavy and takes sometimes upwards of an hour to fully load all the chunks for a 256 chunk render distance), Ecstatic samples the seed in a grid to form more efficient terrain geometry to be rendered at faraway distances. This technique is both lighter on the CPU and GPU for generating and rendering terrain at far distances.

## **Shaders**

At this stage in development, shaders are strongly advised against. Adding shaders is a much bigger project given the way iris exposes its phase system and also the sheer volume of edge cases. Using shaders will cause LOD terrain to not render. There is a toggle to disable this in the "Debug" settings called "Lit vertex format". **You will experience bugs, please do not report them.**   
**Shader support is currently in the works**

## **Q and A**

***

**Question:** 

Does Ecstatic work with Sodium/Embeddium and other similar performance mods?

**Answer:**

**Yes**. It is fully compatable with Sodium, while not required is heavily reccomended to get your performance as high as possible

***

**Question:** 

How high is Ecstatic's render distance by default?

**Answer:**

Ecstatic's render distance by default (100% render distance) is **144 chunks** additionally generated on top of vanilla render distance. It can be raised up to 200% (**292 chunks**), and lowered to 25% (**38 chunks**). Keeping your render distance at 100% is reccomended at the moment, performance loss scales exponentially.

***

**Question:** 

**Does Ecstatic work with terrain generation mods like Terralith or Tectonic?**

**Answer:**

Ecstatic is **compatible with Tectonic** since it doesn't add any new biomes, however performance will be significantly worse, and potentially game breaking. If you have non performance related issues with tectonic, feel free to leave an issue in the issue tracker.

 **Terralith does not have full compatability**. Biomes may look the wrong color or have the wrong trees at a distance, this goes for all mods introducing new biomes.

***

**Question:** 

**How long does it take Ecstatic to generate LOD terrain?**

**Answer:**

> On a new world, it takes about 5 divided by the number of threads you allocated for all the LOD terrain to load in at 100% view distance. Expect about
>
> *   **~1 - 4 minutes** on lower end machines
> *   **~50 seconds** on mid range machines 
> *   **~20 seconds** on high end machines
> *   **~2 seconds** on the latest AMD threadripper

***

**Question:** 

Can you see faraway structures with Ecstatic?

**Yes**, faraway structures should be shown in LOD terrain 

***

**Question:** 

How smooth is Ecstatic with elytra?

**Answer:**

> Medium smooth, gaps in terrain might happen/frame drops so beware!

***

 

## **What's next?**

At the moment the mod is still in beta. For full release we're working on

*   Full shader support through Iris/Oculus
*   Server support
*   Incresed render distance VIA flat Terrain (Up to 1k chunks potentially)
*   Rendering player builds
*   French translation 
*   Better tree system

And we might add later

*   Support for mods with custom biomes
*   Optifine Support
*   Translations to other languages

Made in Canada

Please tell me VIA [the issue tracker at the repo](https://github.com/Wizardtastic/Ecstatic/issues) if there are any features you'd like to see or any bugs you find, any feedback is very appreciated this early on in development. As for now, I'm only doing Forge/Fabric 1.20.1 just for testing purposes, however the future of the mod for the remainder of beta and pre-release is on neoforge 1.21.1.

If you want the source, it is hosted at the source link. As per the liscence you are allowed to modify or remix and distribute the source, however it has to be released under the same CC BY-NC SA 4.0 liscence with attribution!  
  
Fully welcome for use in modpacks, I don't consider monetization from curseforge or modrinth packs/remixes to be "Commercial". Forks that add large meaningful improvements overtop of my mod can also be monetized exclusively on Curseforge, Modrinth, or any other similar modding platform that pays its creators through ad revenue. 

While technically not open source, I went with the liscence since it prevents future derivatives of my mod from being paywalled. It otherwise functions similarly to an open source liscence. I'm not a lawyer, if you have legal advice on how I can better liscence my mod, please leave an issue in the repo.
