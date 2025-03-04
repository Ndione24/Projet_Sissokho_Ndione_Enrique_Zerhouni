package DataBase;
import java.util.ArrayList;
import java.util.List;
public class BufferManager2 {
		
		private List<Frame> bufferpool ;
		//contiendra les frames dont leur pin_count sont passé à 0
		private List<Frame> pagesLRU;

		// constructeur du singleton
				private BufferManager2 ()
				{
				this.pagesLRU = new ArrayList<>(2);	
				this.bufferpool = new ArrayList<Frame>(2);
				//on ajoute deux frames à bufferpool
				Frame f1= new Frame();
				Frame f2= new Frame();
				this.bufferpool.add(f1);
				this.bufferpool.add(f2);
				}
				
				

				
		private static BufferManager2  INSTANCE= new BufferManager2();
		
		public static  BufferManager2 getInstance() {
			return INSTANCE ;
		}
	
		public void infoFrameBufferpool() 
		{
			for(int i=0;i<this.bufferpool.size();i++) 
			{
				System.out.println();
				System.out.println("Frame  "+(i+1) );
				this.bufferpool.get(i).allInfoFrame();
				System.out.println();
			}
		}
		
		
		public void remplirBuffer(PageId pageid, byte[] buff) 
		{
			Frame frame;
			for(int i=0;i<this.bufferpool.size();i++) 
			{
				frame=this.bufferpool.get(i);
				if(frame.getIdDeLaPage().equals(pageid)) 
				{
					frame.setBuffer(buff);
				}
				else 
				{
					System.out.println("pas chargé dans "+i);
				}
			}
			
			
		}
	
		/**
		 * 
		 * @param pageid
		 * @return un tableau de Byte correspondant au buffer de la case
		 */
		/*public byte[] getPage(PageId pageid)
		{
		
			
			for(Frame frame: this.bufferpool)
			{
				
				if(frame.getIdDeLaPage()==null)
				{
					frame.setIdDeLaPage(pageid);
					frame.incrementerPinCount();
					
					
				
					// Signaler que la case a été chargée
					frame.setEstCharge(true);
					
					return frame.getBuffer();
				}
				//si la page existe dans le bufferpool
				else if(frame.getIdDeLaPage().equals(pageid))
				{
					//alors la page existe
					// Si oui,on fait une mise à jour de cette frame
					// incrementer le pin_count
					
					frame.incrementerPinCount();
					
					updateListFrameLRU(pageid);
					
					// Signaler que la case a été chargée
					frame.setEstCharge(true);
					
					return frame.getBuffer();
					
				}
				
				//si la page n'existe pas dans le bufferpool et qu'il y a une case libre
				else if(frame.isEstCharge()==false)
				{
					//on actualise les informations concernant la case
					frame.setIdDeLaPage(pageid);
					frame.incrementerPinCount();
					frame.setEstCharge(true);
					return frame.getBuffer();
					
				}
				//si la page n'existe pas dans le bufferpool et qu'il n y a une case libre
				
				
			}
			
				System.out.println("probleme");
				return null;
				//return meaningLru(pageid);
			
			//return null;
			
		} */
		
		
		
		
		
		
		//refact getPage
		
		
		public byte[] getPage(PageId pageid)
		{
		
			System.out.println("taille du bufferpool"+this.bufferpool.size());
			
			Frame frame;
			byte [] buffRead= new byte[Constants.TAILLEBUFFER];
			for(int i=0;i<this.bufferpool.size();i++)
			{
				frame=this.bufferpool.get(i);
				
				if(frame.getIdDeLaPage()==null)
				{
					System.out.println("on est ici " +i);
					frame.setIdDeLaPage(pageid);
					frame.incrementerPinCount();
					DiskManager.getInstance().readPage(pageid, buffRead);
				    frame.setBuffer(buffRead); 
				    // Signaler que la case a été chargée
					frame.setEstCharge(true);
					return frame.getBuffer();
					
				}
				//si la page existe dans le bufferpool
				else if(frame.getIdDeLaPage().equals(pageid))
				{
					System.out.println("on est ici " +i);
					//alors la page existe
					// Si oui,on fait une mise à jour de cette frame
					// incrementer le pin_count
					
					frame.incrementerPinCount();
					
					updateListFrameLRU(pageid);
					
					DiskManager.getInstance().readPage(pageid, buffRead);
				    frame.setBuffer(buffRead);
					
					// Signaler que la case a été chargée
					frame.setEstCharge(true);
					
					return frame.getBuffer();
					
				}
				
				//si la page n'existe pas dans le bufferpool et qu'il y a une case libre
				else if(frame.isEstCharge()==false)
				{
					System.out.println("on est ici " +i);
					//on actualise les informations concernant la case
					frame.setIdDeLaPage(pageid);
					frame.incrementerPinCount();
					frame.setEstCharge(true);
					DiskManager.getInstance().readPage(pageid, buffRead);
				    frame.setBuffer(buffRead);
					return frame.getBuffer();
					
				}
				//si la page n'existe pas dans le bufferpool et qu'il n y a une case libre
				
				
			}//end for
			
				System.out.println("Go to LRU");
				//return null;
				return meaningLru(pageid);
			//return null;			
		} 
		
		
		//end refact getPage
			
			
			
	
		
		
		
		
		
		
		
		
		
		
		
		
		

		
		/**
		 * <i>Cette méthode permet de libérer une page</i>
		 * 
		 * @param p   <i>La page à libérer</i>
		 * 
		 * @param dirty <i>Correspond au flag dirty(qui specifie si la page a été
		 *              modifiée ou non)</i>
		 */
		
		/**
		 * Methode qui decrememente le pincount d'une case et actualise son flagdirty
		 * @param pageid
		 * @param dirty
		 */
		public void freePage(PageId pageid,int dirty) 
		{
			
			Frame frame;
			for(int i=0;i<this.bufferpool.size();i++) 
			{
				frame=this.bufferpool.get(i);
				
				if(frame.getIdDeLaPage()==null)
				{
					System.out.println("Pas de free, la case est vide "+ i);
					return ;
				}
				
				if(frame.getIdDeLaPage().equals(pageid)) 
				{
					if(frame.getPin_count()>0) 
					{
					frame.decrementerPinCount();
					}
					if(dirty==1) 
					{
						frame.setFlagDirty(dirty);
					}
					
					//apres la decrementation si pincount est égal à 0, on ajoute la frame à la liste LRU
					if(frame.getPin_count()==0) 
					{
						this.pagesLRU.add(frame);
					}
				}
				
				return;
			}
		}

		
		
		
		
		
		
		public byte[] meaningLru(PageId pageid) 
		{
			if(this.pagesLRU.isEmpty())
			{
				
				System.out.println("La Liste LRU est Vide Pas de remplacement possible");
				System.exit(0);
				
			}
			
			/*on cherche la correspondance de la case d'indice 0 se trouvant dans LRU avec le bufferpool
			 * car il s'agira de la case dont le pincount est passé à 0 en premier.
			 * On la supprime par la suite dans le bufferpool ainsi que dans LRU
			 */
			
			PageId pageIdToDeleted=this.pagesLRU.get(0).getIdDeLaPage();
			
			Frame frame;
			byte buffRead []=new byte[Constants.TAILLEBUFFER];
			for(int i=0;i<this.bufferpool.size();i++) 
			{
				frame=this.bufferpool.get(i);
				if(frame.getIdDeLaPage().equals(pageIdToDeleted)) 
				{
					//Si son flagdirty==1 on l'ecrit sur le disque
					if(frame.getFlagDirty()==1) 
					{
						DiskManager.getInstance().writePage(pageIdToDeleted, frame.getBuffer());
					}
					//On renitialise la Frame
					frame.renitialiser();
					//on actualise les informations concernant la case
					
					frame.setIdDeLaPage(pageid);
					frame.incrementerPinCount();
					frame.setEstCharge(true);
					DiskManager.getInstance().readPage(pageid, buffRead);
				    frame.setBuffer(buffRead);
					this.pagesLRU.remove(0);
					return frame.getBuffer();
					
					//Suppression de la frame correspondante dans LRU
					
					
					
				}
			}
			return null;
			
		}
		
		
		
		
		
		
		
		

		/**
		 * Cette méthode permet de supprimer,dans la liste liste Des PagesLRU,
		 * 
		 * la case dont le pin_count n'est plus égal à 0;
		 *
		 * @param PageId 
		 */
		
		private void updateListFrameLRU(PageId page) {
			
			if(this.pagesLRU.size()==0) 
			{
				System.out.println("List LRU vide");
				//pour sortir de la methode
				return;
			}
			if(this.pagesLRU.isEmpty()) 
			{
				System.out.println("List LRU vide");
				//pour sortir de la methode
				return ;
			}
			Frame frame;
			for(int i=0;i<this.pagesLRU.size();i++) 
			{
				frame=this.pagesLRU.get(i);
				if(frame.getIdDeLaPage().equals(page))
				{
					int indexOfFrameToRemove=this.pagesLRU.indexOf(frame);
					this.pagesLRU.remove(indexOfFrameToRemove);
				}
			}

		}
		
		
		

		/**
		 * Cette méthode permet d'écrire toutes les pages dont le flag dirty=1 sur
		 * disque et initialise le flag dirty
		 */
	
		public void FlushAll() 
		{
			for(Frame frame:this.bufferpool) 
			{
				if(frame.getFlagDirty()==1) 
				{
					DiskManager.getInstance().writePage(frame.getIdDeLaPage(), frame.getBuffer());
			
				}
				frame.renitialiser();
				this.pagesLRU.clear();
			}
		}

		/**
		 * Cette méthode initialise les attributs de cette classe
		 */
		public void initialiser() {
			// Initialiser le bufferpool et la liste des page pour LRU
			this.bufferpool.clear();
			this.pagesLRU.clear();

		}
		
		/**
		 * methode qui renitialise completement le bufferPool
		 */
		
		public void reset() 
		{
			for(Frame frame: this.bufferpool) 
			{
				frame.renitialiser();
			}
			
		}

	
}
